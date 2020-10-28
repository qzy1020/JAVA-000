
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;


public class OkHttpClientUtil {
    private static Logger logger = LoggerFactory.getLogger(OkHttpClientUtil.class);
    private String url;
    public OkHttpClientUtil(String url){
        this.url = url;
    }
    public static OkHttpClient client;
    static{
        client = new OkHttpClient.Builder()
                .connectTimeout(3, TimeUnit.SECONDS)
                .readTimeout(3, TimeUnit.SECONDS)
                .writeTimeout(3, TimeUnit.SECONDS)
                .build();
    }
    public void handleRequest(boolean requestPattern){
        Request request = new Request.Builder().get().url(url).build();

        Call call = client.newCall(request);

        if (requestPattern){
            //同步请求
            synRequest(call);
        }else{
            //异步请求
            asynRequest(call);
        }
    }

    public void synRequest(Call call){
        try {
            String resp = call.execute().body().string();
            logger.info("response: "+ resp);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void asynRequest(Call call){
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String resp = response.body().string();
                logger.info("response: "+resp);
            }
        });
    }

    public static void main(String[] args) {
        OkHttpClientUtil clientUser = new OkHttpClientUtil("http://localhost:8808/test");
        clientUser.handleRequest(true);
    }
}
