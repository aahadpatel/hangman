package server;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.Timestamp;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.Scanner;

public class ChatRoom {
	public static Connection con;
	
	public ChatRoom(int port) throws Exception {
		try {
			if(port == 0) System.exit(0);
			
			//String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new java.util.Date());
			
			Date date = new Date();
		    DateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss"); 
			
			System.out.println("Trying to connect to ServerPort: " + port);
			ServerSocket ss = new ServerSocket(port);
			//System.out.println("Connected to port " + port);
			Socket s = ss.accept();
			
			BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
			PrintWriter pw = new PrintWriter(s.getOutputStream());
	
			String username_query = br.readLine();
			String password_query = br.readLine();
			
			System.out.println();
			System.out.println(dateFormat.format(new Date()) + " " + username_query + " - is trying to login with password " + password_query);
			
			//CHECK IF USER IS IN THE DATABASE boolean
			boolean doesExist = checkDatabase(username_query, password_query);			
			String name_of_game = "";
			
			
			//if user is not in database, then add
			if(doesExist == false) {
				pw.println("false");
				pw.flush();
				System.out.println(dateFormat.format(new Date()) + " " + username_query + " - does not have an account so not succesfully logged in. ");
				String insert_into_database = br.readLine();
				if(insert_into_database.equals("insert")) {
					System.out.println(dateFormat.format(new Date()) + " " + username_query + " - created an account with password " + password_query);
					insertIntoDatabase(username_query, password_query, 0, 0);
					System.out.println(dateFormat.format(new Date()) + " " + username_query + " - has record 0 and 0");
					
					//grab name of game
					name_of_game = br.readLine();
					System.out.println(dateFormat.format(new Date()) + " " + username_query + " - wants to start a game called " + name_of_game);
					System.out.println(dateFormat.format(new Date()) + " " + username_query + " - succesfully started game " + name_of_game);
					
					System.out.println(dateFormat.format(new Date()) + " " + name_of_game + " - has 1 player so " + "starting game");
				}
			}
			//if user exists 
			else {
					pw.println("true");
					pw.flush();
					boolean checkPassword = checkValidPassword(username_query, password_query);
					if(checkPassword == true) {
						System.out.println(dateFormat.format(new Date()) + " " + username_query + " - succesfully logged in. ");
						pw.println("password_correct");
						pw.flush();
						String wins = getWins(username_query, password_query);					
						pw.println(wins);
						pw.flush();
						String losses = getLosses(username_query, password_query);
						pw.println(losses);
						pw.flush();
						
						System.out.println(dateFormat.format(new Date()) + " " + username_query + " - has record " + wins + " and " + losses);
						
						//grab name of game
						name_of_game = br.readLine();
						System.out.println(dateFormat.format(new Date()) + " " + username_query + " - wants to start a game called " + name_of_game);
						System.out.println(dateFormat.format(new Date()) + " " + username_query + " - succesfully started game " + name_of_game);
						
						System.out.println(dateFormat.format(new Date()) + " " + name_of_game + " - has 1 player so " + "starting game");
					}
					else {
						System.out.println(dateFormat.format(new Date()) + " " + username_query + " - has an account but not succesfully logged in. ");
						pw.println("password_incorrect");
						pw.flush();
					}
			}
			
			//GAME PLAY GRAB STUFF FROM CLIENT
			String secret_word_string = br.readLine();
			System.out.println("The secret word is " + secret_word_string);
			
			boolean gameIsOn = true;
			String game_start = br.readLine();
			
			if(game_start.equals("game_start")) {
				int guesses_left = 7;
				while(gameIsOn) {
					//System.out.println("Iteration ");
					String letter_guessed = "";
					letter_guessed = br.readLine();
					//System.out.println("Letter guessed: " + letter_guessed);
					
					if(letter_guessed.equals("Winning")) {
						System.out.println(dateFormat.format(new Date()) + " " + name_of_game + " " + username_query + " - You win! " );
						updateDatabaseWin(username_query);
						System.exit(0);
					}
					
					if(letter_guessed.equals("NO_GUESSES_REMAINING")) {
						System.out.println(dateFormat.format(new Date()) + " " + name_of_game + " " + username_query + " - No guesses remaining. You lose! " );
						updateDatabaseWin(username_query);
						System.exit(0);
					}
					if(letter_guessed.length() > 0 && !letter_guessed.equals("NO_GUESSES_REMAINING")){
						
						if(letter_guessed.length() == 1) {
							System.out.println(dateFormat.format(new Date()) + " " + name_of_game + " " + username_query + " - " + letter_guessed + " is not in " + secret_word_string);
							guesses_left--;
							System.out.println(dateFormat.format(new Date()) + " " + name_of_game + " now has " + guesses_left + " guesses remaining. ");
							continue;
						}
						else if(letter_guessed.length() == 2) {
							System.out.println(dateFormat.format(new Date()) + " " + name_of_game + " " + username_query + " - guessed " + letter_guessed.charAt(0));
							System.out.println(dateFormat.format(new Date()) + " " + name_of_game + " " + username_query + " - " + letter_guessed.charAt(0) + " is in " + secret_word_string);
							//continue;
						}
						else {
							System.out.println(dateFormat.format(new Date()) + " " + name_of_game + " " + username_query + "- guessed the word " + letter_guessed);
							String word_guess = br.readLine();
							if(word_guess.equals("WORD_GUESSED_IS_CORRECT")) {
								System.out.println(dateFormat.format(new Date()) + " " + name_of_game + " " + username_query + " - " + letter_guessed + " is correct");
								System.out.println(dateFormat.format(new Date()) + " " + name_of_game + " " + username_query + " wins! ");
								updateDatabaseWin(username_query);
								System.exit(0);
							}
							else if(word_guess.equals("WORD_GUESSED_IS_INCORRECT")) {
								System.out.println(dateFormat.format(new Date()) + " " + name_of_game + " " + username_query + " - " + letter_guessed + " is incorrect");
								System.out.println(dateFormat.format(new Date()) + " " + name_of_game + " " + username_query + " has lost and is no longer in the game. ");
								updateDatabaseLoss(username_query);
								System.exit(0);
							}
						}
					}
				}
			}
			
			s = ss.accept(); // blocking
			System.out.println("Connection from: ");
			//ServerThread st = new ServerThread(s, this);
			//serverThreads.add(st);
		} catch (IOException ioe) {
			System.out.println("ioe in ChatRoom constructor: " + ioe.getMessage());
		}
	}
	
	private void updateDatabaseWin(String username_query) {

		try {
	        String queryString = "Update Players SET wins = wins+1 where username='"+username_query+"'";
	        //set this values using PreparedStatement
	        PreparedStatement ps = con.prepareStatement(queryString);
	        int results = ps.executeUpdate(); //where ps is Object of PreparedStatement

	    } catch (SQLException sql) {
	        System.out.println("Sql exception: " + sql.getMessage());
	    }finally{
	    }
	}
	
	private void updateDatabaseLoss(String username_query) {

		try {
	        String queryString = "Update Players SET losses = losses+1 where username='"+username_query+"'";
	        //set this values using PreparedStatement
	        PreparedStatement ps = con.prepareStatement(queryString);
	        int results = ps.executeUpdate(); //where ps is Object of PreparedStatement

	    } catch (SQLException sql) {
	        System.out.println("Sql exception: " + sql.getMessage());
	    }finally{

	    }
	}

	private String getLosses(String username_query, String password_query) {
		String loss = "";
		try {
	        String queryString = "SELECT losses FROM Players where username='"+username_query+"' and password='"+password_query+"'";
	        //set this values using PreparedStatement
	        PreparedStatement ps = con.prepareStatement(queryString);
	        ResultSet results = ps.executeQuery(); //where ps is Object of PreparedStatement
	        results.next();
	        loss = results.getString("losses");

	    } catch (SQLException sql) {
	        System.out.println("Sql exception: " + sql.getMessage());
	    }finally{

	    }
		return loss;
	}

	//return string wins 
	private String getWins(String username_query, String password_query) {
		String win = "";
		try {
	        String queryString = "SELECT wins FROM Players where username='"+username_query+"' and password='"+password_query+"'";
	        //set this values using PreparedStatement
	        PreparedStatement ps = con.prepareStatement(queryString);
	        ResultSet results = ps.executeQuery(); //where ps is Object of PreparedStatement
	        results.next();
	        win = results.getString("wins");
	        //System.out.println("HELLO: " + results.getString("wins"));

	    } catch (SQLException sql) {
	        System.out.println("Sql exception: " + sql.getMessage());
	    }finally{
	      //closing ResultSet,PreparedStatement and Connection object
	    }
		return win;
	}


	private boolean checkValidPassword(String username_query, String password_query) {

		try {
	        String queryString = "SELECT * FROM Players where username='"+username_query+"' and password='"+password_query+"'";
	        //set this values using PreparedStatement
	        PreparedStatement ps = con.prepareStatement(queryString);
	        ResultSet results = ps.executeQuery(queryString); //where ps is Object of PreparedStatement

	        if(!results.next()) {
	              return false;
	        }

	    } catch (SQLException sql) {
	        System.out.println("Sql exception: " + sql.getMessage());
	    }finally{

	    }
		return true;
	}

	//Method "insertDatabase()" inserts a filename, score pair into the database
	public void insertIntoDatabase(String username_query, String password, int wins, int losses) throws Exception {
		try {
			PreparedStatement posted = con.prepareStatement("INSERT INTO Players(username, password, wins, losses) VALUES ('"+username_query+"', '"+password+"', '"+wins+"','"+losses+"')");
			posted.executeUpdate();
		} catch(Exception e) {
			System.out.println(e.getMessage());
		} 
	}

	
//	public void broadcast(String message, ServerThread st) {
	public void broadcast(ChatMessage cm, ServerThread st) {
		//if (message != null) {
//		if (cm != null) {
//			//System.out.println(message);
//			System.out.println(cm.getUsername() + ": " + cm.getMessage());
//			for(ServerThread threads : serverThreads) {
//				if (st != threads) {
//					//threads.sendMessage(message);
//					threads.sendMessage(cm);
//				}
//			}
//		}
	}
	
	//method to check whether or not user is in the database
	public static boolean checkDatabase(String username_query, String password) throws SQLException {
		String queryCheck = "SELECT * from Players WHERE username =  + '" + username_query + "'";
		if(con != null) {
			Statement st = con.createStatement();
			ResultSet rs = st.executeQuery(queryCheck); // execute the query, and get a java resultset
			if(rs.absolute(1)) {
			     return true;
			}
			else return false;
		} else return false;
	}
	
	private static Connection getConnection(String dbConnection, String dbUsername, String dbPassword) {
		try
	    {
	      String driver = "com.mysql.jdbc.Driver";
	      Class.forName(driver);
	      //create the connection
	      con = DriverManager.getConnection(dbConnection+"?user="+dbUsername+"&password="+dbPassword+"useSSL=false", dbUsername, dbPassword);
	      System.out.print("Trying to connect to database...Connected! ");
	      return con;
	    } catch (SQLException e) {
	    	//System.out.println(e.getMessage());
	    	System.out.println("Trying to connect to database...Unable to connect to database " + dbConnection + " with username " + dbUsername + " and password " + dbPassword + ". " );
	    	System.exit(0);
	    } catch(ClassNotFoundException e) {
	    	System.out.println(e.getMessage());
	    } 
		return null;
	}

	private static int parseConfigurationFile() {
		// TODO Auto-generated method stub
		boolean configIsValid = true;
		String serverHostName = "";
		String serverPort = "";
		String dbConnection = "";
		String dbUsername = "";
		String dbPassword = "";
		String secretWordFile = "";

		while(configIsValid == true) {
			FileReader fileReader = null;
			String filename = "";
			try {
				Scanner scanner = new Scanner(System.in);
				System.out.print("What is the name of the configuration file? ");
				filename = scanner.nextLine();
				fileReader = new FileReader(filename);
				System.out.println("Reading configuration file...");
				Properties p = new Properties();
				p.load(fileReader);
				serverHostName = p.getProperty("ServerHostname");
				serverPort = p.getProperty("ServerPort");
				dbConnection = p.getProperty("DBConnection");
				dbUsername = p.getProperty("DBUsername");
				dbPassword = p.getProperty("DBPassword");
				secretWordFile = p.getProperty("SecretWordFile");
				
				if(serverHostName == null) {
					System.out.println("ServerHostname is a required parameter in the configuration file");
					System.out.println();
					configIsValid = false;
				}
				if(serverPort == null) {
					System.out.println("ServerPort is a required parameter in the configuration file");
					System.out.println();
					configIsValid = false;
				}
				if(dbConnection == null) {
					System.out.println("DBConnection is a required parameter in the configuration file");
					System.out.println();
					configIsValid = false;
				}
				if(dbUsername == null) {
					System.out.println("DBUsername is a required parameter in the configuration file");
					System.out.println();
					configIsValid = false;
				}
				if(dbPassword == null) {
					System.out.println("DBPassword is a required parameter in the configuration file");
					System.out.println();
					configIsValid = false;
				}
				if(secretWordFile == null) {
					System.out.println("SecretWordFile is a required parameter in the configuration file");
					System.out.println();
					configIsValid = false;
				}
				
				if(configIsValid == true) {
					System.out.println("ServerHostname - " + serverHostName);
					System.out.println("ServerPort - " + serverPort);
					System.out.println("DBConnection - " + dbConnection);
					System.out.println("DBUsername - " + dbUsername);
					System.out.println("DBPassword - " + dbPassword);
					System.out.println("SecretWordFile - " + secretWordFile);
					System.out.println();
					
					//CONNECT TO DATABASE
					if(configIsValid == true) {
						getConnection(dbConnection, dbUsername, dbPassword);
						int new_port = Integer.parseInt(serverPort);
						return new_port;
					} else {
						System.out.println("Please fix the configuration file and restart the program. ");
					}
					
					//break out of while loop after database connection
					configIsValid = false;
				}
				
			} catch(FileNotFoundException fnfe) {
				System.out.println(filename + " cannot be found.");
			} catch(IOException ioe) {
				System.out.println("IOException");
			}
			finally {
				if(fileReader != null) {
					try {
						fileReader.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		return 0;
	}
	
	public static void main(String [] args) throws Exception {
		//parsing the configuration file
		int new_port = parseConfigurationFile();
		ChatRoom cr = new ChatRoom(new_port);
	}
}