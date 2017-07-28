package controllers;

import dao.RequestDao;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import model.Request;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

import static io.netty.handler.codec.http.HttpHeaders.Names.*;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;


public class ServerHandler extends SimpleChannelInboundHandler<HttpObject> {

    private HttpRequest request;
    String userId;
    String externalUserId;
    String dspId;
    private RequestDao requestDao;
    Properties prop;

    {
        prop = new Properties();
        try {
            prop.load(ServerHandler.class.getClassLoader().getResourceAsStream("config/config.properies"));
        } catch (IOException e) {
            System.out.println("can't find config file");
        }
        requestDao = new RequestDao(prop);
    }


    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) {
        if (msg instanceof HttpRequest) {
            HttpRequest request = this.request = (HttpRequest) msg;
            QueryStringDecoder queryStringDecoder = new QueryStringDecoder(request.getUri());
            if (!queryStringDecoder.path().equalsIgnoreCase(prop.getProperty("address"))) {
                return;
            }
            Map<String, List<String>> params = queryStringDecoder.parameters();
            if (!params.isEmpty()) {
                for (Map.Entry<String, List<String>> p: params.entrySet()) {
                    String key = p.getKey();
                    List<String> values = p.getValue();
                    if ("externalUserId".equals(key)) {
                        externalUserId = values.get(0);
                    } else if ("dspId".equals(key)){
                        dspId = values.get(0);
                    }
                }
            }
        }
        if (msg instanceof LastHttpContent) {
            userId = getUserId(ctx);
            if (dspId != null && externalUserId != null) {
                saveRequest(dspId, userId, externalUserId);
            }
            ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        }
    }

    private void saveRequest(String dspId, String userId, String externalUserId) {
        Request req = new Request(dspId, userId, externalUserId);
        requestDao.save(req);
    }

    private String getUserId(ChannelHandlerContext ctx) {
        String userId = null;
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK);
        response.headers().set(CONTENT_TYPE, "text/plain; charset=UTF-8");
        String cookieString = request.headers().get(COOKIE);
        if (cookieString != null) {
            Set<Cookie> cookies = CookieDecoder.decode(cookieString);
            if (!cookies.isEmpty()) {
                for (Cookie cookie: cookies) {
                    response.headers().add(SET_COOKIE, ServerCookieEncoder.encode(cookie));
                    if ("userId".equals(cookie.getName())) {
                        userId = cookie.getValue();
                        break;
                    }
                }
            }
        } 
        if (userId == null) {
            userId = generateNewUserId();
            response.headers().add(SET_COOKIE, ServerCookieEncoder.encode("userId", userId));
        }

        ctx.write(response);
        return userId;
    }

    private String generateNewUserId() {
        return String.valueOf(new Random().nextLong());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
