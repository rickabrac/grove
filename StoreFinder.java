//
// StoreFinder.java - Grove Collaborative coding challenge 
//
// Rick Tyler
// 1015 Rose Avenue
// Oakland, CA 94611
// (510) 910-6536
// rick.tyler@gmail.com
//

import java.io.*;
import java.net.*;
import java.util.*;
import org.json.simple.parser.JSONParser;
import org.json.simple.*;

public class StoreFinder
{
	private String address = null;
	private String zip = null;
	private String units = "mi";
	private String output = "text";

	static public void main ( String[] args )
	{
		StoreFinder finder = new StoreFinder();
		finder.run( args );
	}

	// computeDistance() method adapted from https://www.geodatasource.com/developers/java

	private double computeDistance( double lat1, double lon1, double lat2, double lon2, String units )
	{
		if( (lat1 == lat2) && (lon1 == lon2) )
			return( 0 );
		else
		{
			double theta = lon1 - lon2;
			double dist = Math.sin( Math.toRadians( lat1 ) ) * Math.sin( Math.toRadians( lat2 ) )
				+ Math.cos( Math.toRadians( lat1 ) ) * Math.cos( Math.toRadians( lat2 ) )
				* Math.cos( Math.toRadians( theta ) );
			dist = Math.acos( dist );
			dist = Math.toDegrees( dist );
			dist = dist * 60 * 1.1515;
			if( units.equals( "km" ) )
				dist = dist * 1.609344;
			else 
				dist = dist * 0.8684;
			return( dist );
		}
	}

	// JSON query convenience method

	private JSONObject jsonQuery ( String urlString, String requestMethod, JSONObject input ) throws Exception
	{
		long responseCode = 0;
		HttpURLConnection https = null;
		StringBuilder sb = new StringBuilder();  
		try
		{
			URL url = new URL( urlString ); 
			https = (HttpURLConnection) url.openConnection();
			https.setRequestMethod( requestMethod ); 
			https.setRequestProperty( "Content-Type", "application/json" );
			https.setRequestProperty( "Accept", "application/json" );
			https.setRequestProperty( "Content-Length",  "" + (input == null ? "0" : input.toString().length()) );
			https.setDoOutput( true );
			https.setDoInput( true );
			if( input != null )
			{
				OutputStreamWriter wr = new OutputStreamWriter( https.getOutputStream() );
				wr.write( input.toString());
				wr.flush();
			}
			responseCode = https.getResponseCode();
		}
		catch( Exception e )
		{
			System.out.println( "JSONRequest.query EXCEPTION: " + e.getMessage() );
			return( null );
		}
		try
		{
			BufferedReader br = new BufferedReader( new InputStreamReader(
				responseCode < 202 ? https.getInputStream() : https.getErrorStream(),
				"utf-8" ) );  
			String line = null;  
			while( (line = br.readLine() ) != null )
				sb.append( line ); // + "\n" ); 
			br.close();  
		}
		catch ( Exception e )
		{
			System.out.println( "JSONRequest.query EXCEPTION: " + e.getMessage() );
		}
		JSONParser jsonParser = new JSONParser();
		if( jsonParser == null )
		{
			throw( new Exception( "new JSONParser() failed" ) );
		}
		Object obj = null;
		try
		{
			obj = jsonParser.parse( sb.toString() );
		}
		catch( Exception e )
		{
			System.out.println( "JSONParser Exception: " + e.getMessage() );
		}
		JSONObject jsonOutput = null;
		if( obj != null )
			jsonOutput = (JSONObject) obj;
		if( jsonOutput == null )
		{
			throw( new Exception( "jsonOutput is null" ) );
		}
		if( responseCode == 404 )
			return( null );
		if( responseCode > 201 )
		{
			if( jsonOutput.get( "errors" ) == null )
			{
				// do nothing
			}
			else if( jsonOutput.get( "errors" ) instanceof String )
			{
				String errors = (String) jsonOutput.get( "errors" );
				throw( new Exception( errors ) );
			}
			else if( jsonOutput.get( "errors" ) instanceof JSONObject )
			{
				JSONObject errors = (JSONObject) jsonOutput.get( "errors" );
				JSONArray base = (JSONArray) errors.get( "base" );
				if( base == null )
				{
					throw( new Exception( "errors.base not returned" ) );
				}
				String error = "";
				for( int i = 0; i < base.size(); i++ )
				{
					if( error.length() > 0 )
						error += " ";
					String errorFragment = (String) base.get( i );
					error += errorFragment + ".";
				}
				throw( new Exception( error ) ); 
			}
			throw( new Exception( "responseCode=" + responseCode ) );
		}
		return( jsonOutput );
	}

	public void run ( String[] args )
	{
		try
		{
			for( String arg : args )
			{
				String [] part = arg.split( "=" );
				if( part.length == 2 && part[ 0 ].substring( 0, 2 ).equals( "--" ) )
				{
					// valid argument prefix syntax (e.g. --foo=?)
					switch( part[ 0 ] )
					{
						case "--zip":
						{
							zip = part[ 1 ];
							break;
						}
		
						case "--address":
						{
							address = part[ 1 ];
							break;
						}
		
						case "--units":
						{
							if( !part[ 1 ].equals( "km" ) && !part[ 1 ].equals( "mi" ) )
								throw( new Exception( "units must be 'km' or 'mi'" ) );
							units = part[ 1 ];
							break;
						}
		
						case "--output":
						{
							if( !part[ 1 ].equals( "text" ) && !part[ 1 ].equals( "json" ) )
								throw( new Exception( "output must be 'text' or 'json'" ) );
							output = part[ 1 ];
							break;
						}

						default:
						{
							throw( new Exception( "invalid argument (" + part[ 0 ] + "=" + part[ 1 ] + ")" ) );
						}
					}
				}
			}

			if( zip == null && address == null )
				throw( new Exception( "You must specify a zip code or address to find a store." ) );

			if( zip != null && address != null )
			{
				System.out.printf( "ignoring --zip\n" );
				zip = null;
			}
		}
		catch( Exception e )
		{
			System.out.printf( "\nError: " + e.getMessage() + "\n" );
			try
			{
				FileReader fr = new FileReader( "./usage.txt" );
				BufferedReader br = new BufferedReader( fr ); 
				String line;
				while( (line = br.readLine()) != null )
					System.out.println( line );
			}
			catch( Exception _e )
			{
				System.out.println( e.getMessage() ); 
			}
			System.exit( -1 );
		}

		// use positionstack api to fetch longitude and latitude for input address or zip

		try
		{
			JSONObject json = jsonQuery(
				"http://api.positionstack.com/v1/forward?access_key=fdf95f3a41f431de08d28ea97237f731&query="
				+ URLEncoder.encode( zip != null ? zip : address, "UTF-8" ),
				"GET", null );
			if( json == null )
			{
				System.out.println( "positionstack.com api failed - please try again" );
				System.exit( -1 );
			}
			JSONArray data = (JSONArray) json.get( "data" );
			if( data == null || data.size() == 0 )
			{
				System.out.println( "Unknown address: " + address );
				System.exit( -1 );
			}
			JSONObject location = null;
			for( int i = 0; i < data.size(); i++ )
			{
				location = (JSONObject) data.get( i );
				if( location == null || location.get( "longitude" ) == null )
				{
					System.out.println( "Unknown location: " + address );
					System.exit( -1 );
				}
				break;
			}
			double latitude = (double) location.get( "latitude" );
			double longitude = (double) location.get( "longitude" );

			// scan store-locations.csv
			File file = new File( "./store-locations.csv" );
			Scanner scanner = new Scanner( file );
			ArrayList< HashMap< String, String > > locations = new ArrayList< HashMap< String, String > >(); 
			double distance = -1;
			long lines = 0;
			String store = null;
			JSONObject jsonStore = null; 
			while( scanner.hasNextLine() )
			{
				String line = scanner.nextLine();
				if( lines++ == 0 )
					continue;
				ArrayList<String> result = new ArrayList<String>();
				int start = 0;
				boolean inQuotes = false;
				for( int current = 0; current < line.length(); current++ )
				{
					if( line.charAt( current ) == '\"' )
						inQuotes = !inQuotes; // toggle state
					else if( line.charAt( current ) == ',' && !inQuotes )
					{
						result.add( line.substring( start, current ) );
						start = current + 1;
					}
				}
				result.add( line.substring( start ) );
				double _latitude = Double.parseDouble( result.get( 6 ) );
				double _longitude = Double.parseDouble( result.get( 7 ) );
				double _distance = computeDistance( latitude, longitude, _latitude, _longitude, units ); 
				if( distance == -1 || _distance < distance )
				{
					store = result.get( 0 ) + ", "
						+ result.get( 1 ) + ", " + result.get( 2 ) + ", " + result.get( 3 ) + ", "
						+ result.get( 4 );
					distance = _distance;
					Map< String, String > jsonMap = new HashMap<>();
					jsonMap.put( "name", result.get( 0 ) );
					jsonMap.put( "location", result.get( 1 ) );
					jsonMap.put( "address", result.get( 2 ) );
					jsonMap.put( "city", result.get( 3 ) );
					jsonMap.put( "state", result.get( 4 ) );
					jsonMap.put( "zip", result.get( 6 ) );
					jsonMap.put( "latitude", result.get( 6 ) );
					jsonMap.put( "longitude", result.get( 7 ) );
					jsonMap.put( "county", result.get( 8 ) );
					jsonMap.put( "distance", String.format( "%.02f", distance ) ); 
					jsonStore = new JSONObject( jsonMap );
				}
			}
			if( output.equals( "text" ) )
				System.out.printf( "%s (%.02f %s)\n", store, distance, units.equals( "km" ) ? "kilometers" : "miles" );
			else
				System.out.printf( "%s\n", jsonStore.toString() );
		}
		catch( Exception e )
		{
			System.out.println( "EXCEPTION: " + e.getMessage() );
		}
	}
}

