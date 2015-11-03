package com.example.videomonitor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.example.videomonitor.R;

import android.app.Fragment;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;

public class HomeFragment extends Fragment {
	private WebView webView;
	SearchView searchView;
	

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		 setHasOptionsMenu(true);
	}


	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View homeView = inflater.inflate(R.layout.home_layout,container, false);
		webView=(ProgressWebView)homeView.findViewById(R.id.web_view);
		 WebSettings settings = webView.getSettings();  
	        settings.setSupportZoom(true);          //支持缩放  
	        settings.setBuiltInZoomControls(true); 
	        settings.setJavaScriptEnabled(true); //启用内置缩放装置  
	        webView.setWebViewClient(new WebViewClient() {  
	            //当点击链接时,希望覆盖而不是打开新窗口  
	            @Override  
	            public boolean shouldOverrideUrlLoading(WebView view, String url) {  
	                view.loadUrl(url);  //加载新的url  
	                return true;    //返回true,代表事件已处理,事件流到此终止  
	            }  
	        }); 
	        webView.setOnKeyListener(new View.OnKeyListener() {  
	            @Override  
	            public boolean onKey(View v, int keyCode, KeyEvent event) {  
	                if (event.getAction() == KeyEvent.ACTION_DOWN) {  
	                    if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {  
	                        webView.goBack();   //后退  
	                        return true;    //已处理  
	                    }  
	                }  
	                return false;  
	            }  
	        });  
	        webView.loadUrl("http://222.25.140.1:8082/WebVideo/login_User.action?username=SXUser1&" +
	        		"password=SXUser1&width=800&height=600");
		return homeView;
	}
	@Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // TODO Auto-generated method stub
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.home_menu,menu);
        MenuItem searchItem = menu.findItem(R.id.menu_item_search);  
        searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint("请输入搜索内容");
        searchView.setOnQueryTextListener(new OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String queryText) {
                return true;
            }
            @Override
            public boolean onQueryTextSubmit(String queryText) {
                if (searchView != null) {
                	if(isHomepage(queryText))
                		webView.loadUrl(queryText);
                	else
                		if(isDomainpage(queryText))
                			webView.loadUrl("http://"+ queryText);
                		else
                			webView.loadUrl("http://www.baidu.com/s?wd="+queryText);
                	searchView.clearFocus(); // 不获取焦点
                }
                return true;
            }
        });
    }
	  public static boolean isHomepage( String str ){
	        String regex = "http://(([a-zA-z0-9]|-){1,}\\.){1,}[a-zA-z0-9]{1,}-*" ;
	        return match( regex ,str );
	    }
	  public static boolean isDomainpage( String str ){
	        String regex = "(([a-zA-z0-9]|-){1,}\\.){1,}[a-zA-z0-9]{1,}-*" ;
	        return match( regex ,str );
	    } 
	    private static boolean match( String regex ,String str ){
	        Pattern pattern = Pattern.compile(regex);
	        Matcher  matcher = pattern.matcher( str );
	        return matcher.matches();
	    }
      
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        switch(item.getItemId()){
        case R.id.menu_item_search:
        	return true;
        default:
        	return super.onOptionsItemSelected(item);
        }
        
    }

}
