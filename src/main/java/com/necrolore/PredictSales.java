package com.necrolore;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

@SuppressWarnings("serial")
@WebServlet("/sales")
public class PredictSales extends HttpServlet {
	
	//GET http://store.steampowered.com/api/appdetails/
	private static String APP_DETAILS_URL = "http://store.steampowered.com/api/appdetails/";

	  @Override
	  public void doGet(HttpServletRequest request, HttpServletResponse response) {
		  
		int gameId = -1;
		try {
			gameId = Integer.parseInt(request.getParameter("gameId"));
		}
		catch (Exception e){
			this.goToLocal(request, response,  "/error?message="+e.getMessage());
		}
		
		// Create a parser for jsons
		JSONParser parser = new JSONParser();
		
		// Check if the game exists
		JSONObject gameInfo = null;
		try {
			JSONObject responseJSON = (JSONObject) parser.parse(this.getRequest(APP_DETAILS_URL+"?appids="+gameId));
			gameInfo = (JSONObject) responseJSON.get(Integer.toString(gameId));
		} catch (Exception e) {
			this.goToLocal(request, response,  "/error?message="+e.getMessage());
		}
		
		if((boolean) gameInfo.get("success"))
			this.predictSales(request, response, gameInfo);
		else 
			this.goToLocal(request, response, "/?fail="+gameId);

	  }
	  
	  private void predictSales(HttpServletRequest request, HttpServletResponse response, JSONObject gameInfo){
		  this.goToLocal(request, response, "/success");
	  }
	  
	  private void goToLocal(HttpServletRequest request, HttpServletResponse response, String dest) {
		  try {
			response.sendRedirect(request.getContextPath() + dest);
		  } catch (IOException e) {
			try {
				response.sendRedirect(request.getContextPath() + "/error?message="+e.getMessage());
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		  }
	  }
	  
	  private String getRequest(String url) throws IOException {
		  return this.getRequest(url, null);
	  }
	  
	  private String getRequest(String url, String cookie) throws IOException {
		  
		// Setup a GET request
		HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
		if(cookie!=null)
			connection.setRequestProperty("Cookie", cookie);
		
		// Check for redirect
		int status = connection.getResponseCode();
		if(status != HttpURLConnection.HTTP_OK && 
				(status == HttpURLConnection.HTTP_MOVED_TEMP
				|| status == HttpURLConnection.HTTP_MOVED_PERM
					|| status == HttpURLConnection.HTTP_SEE_OTHER)) {
			// return the get request with the redirect
			return this.getRequest(connection.getHeaderField("Location"), 
								connection.getHeaderField("Set-Cookie"));
		}
		
		// Get the response and return it
		StringBuffer response = new StringBuffer();
		String line;
		BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		while ((line = reader.readLine()) != null) {
			response.append(line);
		}
		reader.close();
		return response.toString();
		
			
			
	  }
}
