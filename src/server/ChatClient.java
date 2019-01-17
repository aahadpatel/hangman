package server;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;
import java.util.Random;
import java.util.Scanner;

public class ChatClient extends Thread {

	private BufferedReader br;
	private PrintWriter pw;
	private ObjectInputStream ois;
	private ObjectOutputStream oos;
	public ChatClient(String hostname, int port) {
		try {
			
			///
			System.out.println("Trying to connect to " + hostname + ":" + port);
			Socket s = new Socket(hostname, port);
			System.out.println("Connected to " + hostname + ":" + port);
			System.out.println();
			
			Scanner scanner = new Scanner(System.in);
			System.out.print("Username: ");
			String username = scanner.nextLine();
			System.out.print("Password: ");
			String password = scanner.nextLine();
			
			br = new BufferedReader(new InputStreamReader(s.getInputStream()));
			pw = new PrintWriter(s.getOutputStream());
			pw.println(username.trim());
			pw.flush();
			pw.println(password.trim());
			pw.flush();
			
			String accountExists = br.readLine();
			
			String wins = "";
			String losses = "";
			
			if(accountExists.equals("false")) {
				System.out.println();
				System.out.print("No account exists with those credentials.\n\nWould you like to create a new account? (yes/no) ");
				Scanner scan = new Scanner(System.in);
				String user_response = scan.nextLine();
				if(user_response.toLowerCase().trim().equals("yes")) {
					System.out.println();
					System.out.print("Would you like to use the username and password above? ");
					Scanner scan1 = new Scanner(System.in);
					String user_response_username_password = scan1.nextLine();
					if(user_response_username_password.toLowerCase().equals("yes")) {
						pw.println("insert");
						pw.flush();
						System.out.println("Great! You are now logged in as " + username + ". ");
						System.out.println();
						System.out.println(username + "'s Record");
						System.out.println("----------------");
						wins = "0";
						losses = "0";
						int wins_integer = Integer.parseInt(wins);
						int losses_integer = Integer.parseInt(losses);
						System.out.println("Wins- " + wins);
						System.out.println("Losses- " + losses);
						System.out.println();
					}
				}
			} 
			//there is an account
			else if(accountExists.equals("true")) {
				//boolean right_password = false;
					String passwordValid = br.readLine();
					if(passwordValid.equals("password_correct")) {
						System.out.println("Great! You are now logged in as " + username + ". ");
						System.out.println();
						System.out.println(username + "'s Record");
						System.out.println("----------------");
						//right_password = true;
						wins = br.readLine();
						int wins_int = Integer.parseInt(wins);
						System.out.println("Wins- " + wins_int);
						//losses
						losses = br.readLine();
						int losses_int = Integer.parseInt(losses);
						System.out.println("Losses- " + losses_int);
						System.out.println();
					}
					else if(passwordValid.equals("password_incorrect")) {
						System.out.println();
						System.out.println("Incorrect password...");
						System.out.println("Exiting the program. Please provide a valid password next time or create a new account. ");
						System.exit(0);
					}
			}
			
			//start game and join game option
			System.out.println("\t1) Start a game");
			System.out.println("\t2) Join a game");
			System.out.println();
			System.out.print("Would you like to start a game or join a game? ");
			//String str = br.readLine();
			//int choice = Integer.parseInt(str);
			Scanner scan = new Scanner(System.in);
			int start_or_join_choice = scan.nextInt();

			if(start_or_join_choice == 1) {
				startGame(username, wins, losses);
			}
			else {
				joinGame();
			}
			
		} catch (IOException ioe) {
			System.out.println("Unable to connect to server " + hostname + " on port " + port);
		}
	}
	public void joinGame() {
		// TODO Auto-generated method stub
	}
	public void startGame(String username, String win_string, String loss_string) {
		int win_int = Integer.parseInt(win_string);
		int loss_int = Integer.parseInt(loss_string);
		// TODO Auto-generated method stub
		System.out.println();
		System.out.print("What is the name of the game? ");
		Scanner scan = new Scanner(System.in);
		String name_of_game = scan.nextLine();interrupt();
		
		//send name of game
		pw.println(name_of_game);
		pw.flush();
		
		System.out.println();
		System.out.print("How many users will be playing (1)? ");
		boolean one_player = true;
		while(one_player) {
			Scanner scanner = new Scanner(System.in);
			int num_players = scanner.nextInt();
			if(num_players != 1) {
				System.out.println("Only single player functionality works. Please enter 1. ");
			}
			else {
				System.out.println();
				System.out.println("All users have joined. ");
				System.out.println();
				System.out.println("Determining secret word...");
				try {
					playGame(username, win_int, loss_int);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}	
		}	
	}
	//single player gameplay
	public void playGame(String u_name, int wins_integer, int losses_integer) throws IOException {
		// TODO Auto-generated method stub
		//server
				String filename = "hangmanwords.txt";
				FileReader fileReader = new FileReader(filename);
		        BufferedReader bufferedReader = new BufferedReader(fileReader);
		        ArrayList<String> lines = new ArrayList<String>();
		        String line = null;
		        while ((line = bufferedReader.readLine()) != null) {
		            lines.add(line);
		        }
		        bufferedReader.close();
		          
				Scanner scanner = new Scanner(System.in);
				Random random_word = new Random();
				//get a random index
				int random_index = random_word.nextInt(lines.size());
				String word = lines.get(random_index);
								
				pw.println(word);
				pw.flush();	
				boolean gameRunning = true;
				int guesses_left = 7;
				while(gameRunning) {
					pw.println("game_start");
					pw.flush();
					System.out.println("Hangman Single-Player Game");
					char[] chosen_word = word.toCharArray();
					
					
					
					//player
					char[] player = new char[chosen_word.length];
					//underscore
					for(int i = 0; i < player.length; i++) {
						player[i] = '_';
					}
					
					int count = 0;
					boolean wordIsGuessed = false;
					
					while(count != guesses_left && !wordIsGuessed) {
						//System.out.println("Current guesses: ");
						for(int i = 0; i < player.length; i++) {
							System.out.print(player[i] + " ");
						}
						System.out.println();
						System.out.println("You have " + guesses_left + " incorrect guesses remaining. ");
						System.out.println("\t1) Guess a letter.\n\t2) Guess a word. ");
						System.out.println();
						System.out.print("What would you like to do? ");
						System.out.println();
						Scanner scan = new Scanner(System.in);
						int guess_letter_or_word = scan.nextInt();
						//if user guesses a letter
						if(guess_letter_or_word == 1) {
							System.out.print("Letter to guess- ");
							Scanner new_scanner = new Scanner(System.in);
							char input = scanner.nextLine().charAt(0);
							
							//pw.println(input);
							//pw.flush();
							
							//user inputs something valid
							boolean letter_in_word = false;
							boolean a_letter_been_replaced = false;
							for(int i = 0; i < chosen_word.length; i++) {
								if(chosen_word[i] == input) {
									player[i] = input;
									letter_in_word = true;
									
									String combine = "";
									combine = combine + input + input;
									if(a_letter_been_replaced == false) {
										pw.println(combine);
										pw.flush();
										a_letter_been_replaced = true;
									}
								}
								else if(i == chosen_word.length-1) {
									if(letter_in_word == false) {
										pw.println(input);
										pw.flush();
										guesses_left--;
									}
								}
							}
							boolean isComplete = true;
							for(int i = 0; i < player.length; i++) {
								if(player[i] == '_') {
									isComplete = false;
								}
							}
							if(isComplete == true) {
								wordIsGuessed = true;
								System.out.println("You won! ");
								pw.println("Winning");
								pw.flush();
								System.out.println();
								displayWinsLosses(u_name, wins_integer+1, losses_integer);
								System.exit(0);
							}
						}
						else {
							System.out.print("Word to guess- ");
							Scanner new_scanner = new Scanner(System.in);
							String word_guess = new_scanner.nextLine();
							char[] user_response_to_char_array = word_guess.toCharArray();
							boolean isSame = Arrays.equals(user_response_to_char_array, chosen_word);
							if(isSame == true) {
								pw.println(word_guess);
								pw.flush();
								System.out.println("That is correct! You win! ");
								pw.println("WORD_GUESSED_IS_CORRECT");
								pw.flush();
								System.out.println();
								displayWinsLosses(u_name, wins_integer+1, losses_integer);
								System.exit(0);
							} 
							else if(isSame == false) {
								pw.println(word_guess);
								pw.flush();
								System.out.println("That is incorrect! You lose :(");
								pw.println("WORD_GUESSED_IS_INCORRECT");
								pw.flush();
								System.out.println();
								displayWinsLosses(u_name, wins_integer, losses_integer+1);
								System.exit(0);
							}
						}
					}
					if(wordIsGuessed == false) {
						System.out.println("No guesses remaining! ");
						pw.println("NO_GUESSES_REMAINING");
						pw.flush();
						System.out.println();
						displayWinsLosses(u_name, wins_integer, losses_integer+1);
						System.exit(0);
					}
				}
	}
	private void displayWinsLosses(String userlogin, int wins_integer, int losses_integer) {
		System.out.println(userlogin + "'s Record");
		System.out.println("----------------");
		//right_password = true;
		System.out.println("Wins- " + wins_integer);
		//losses
		System.out.println("Losses- " + losses_integer);
		System.out.println();
		System.out.println("Thank you for playing Hangman! ");
		
	}
	public void run() {
		try {
			while(true) {
				Scanner scan = new Scanner(System.in);
				ChatMessage cm = (ChatMessage)ois.readObject();
				String title = cm.getTitle();
				String desc = cm.getDescription();
				if(title.equals("turn")) {
					String line = scan.nextLine();
					//char input = scan.nextLine().charAt(0);
					ChatMessage send = new ChatMessage("guess", line);
					oos.writeObject(send);
					oos.flush();
				}
				else {
					
				}
				//String line = br.readLine();
				//System.out.println(line);
			}
		} catch (IOException ioe) {
			System.out.println("ioe in ChatClient.run(): " + ioe.getMessage());
		} catch (ClassNotFoundException cnfe) {
			System.out.println("cnfe: " + cnfe.getMessage());
		}
	}
	
	private static void getConnection(String dbConnection, String dbUsername, String dbPassword) {
		// TODO Auto-generated method stub
//		String dbPass = "";
//		String connString = String.format(dbConnection, dbPassword);
//		java.sql.Connection conn = null;
//		Statement st = null;
//		try {
//			// connect to database
//			Class.forName("com.mysql.jdbc.Driver");
//			conn = DriverManager.getConnection(connString);
//			st = conn.createStatement();
//			System.out.println("Connection made! ");
//
//			// access database
//			st.executeUpdate("USE hangmanDatabase;");
//			
//		} catch (SQLException e) {
//			System.out.println(e.getMessage());
//		} catch (ClassNotFoundException e) {
//			System.out.println(e.getMessage() + "add to waitlist");
//		} finally {
//			try {
//				if (conn != null) {
//					conn.close();
//				}
//			} catch (SQLException e) {
//				System.out.println(e.getMessage());
//			}
//		}
		
		try
	    {
			Connection con;
	      //In order to create a connection to MySQL, the following are required: driver, url, username, and password
	      String driver = "com.mysql.jdbc.Driver";
	      String url = dbConnection;
	      Class.forName(driver);
	      //create the connection
	      con = DriverManager.getConnection(dbConnection+"?user="+dbUsername+"&password="+dbPassword+"useSSL=false", dbUsername, dbPassword);
	      System.out.print("Connection made! ");
	    } catch (SQLException e) {
	    	System.out.println(e.getMessage());
	    } catch(ClassNotFoundException e) {
	    	System.out.println(e.getMessage());
	    } 
	}
	
	//START GAME AND JOIN GAME FUNCTIONS HERE
	
	public static void main(String [] args) {
		//parse the config file 
		String serverHostName = "";
		String serverPort = "";
		String dbConnection = "";
		String dbUsername = "";
		String dbPassword = "";
		String secretWordFile = "";
		
		boolean configIsValid = true;
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
					} else {
						System.out.println("Please fix the configuration file and restart the program. ");
						System.exit(0);
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
		int new_port = Integer.parseInt(serverPort);
		ChatClient cc = new ChatClient(serverHostName, new_port);
	}
}