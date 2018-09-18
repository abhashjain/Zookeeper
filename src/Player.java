import java.util.Scanner;
import org.apache.zookeeper.ZooKeeper;

public class Player {
	public static final String BASE_PATH = "/ajain28";
	//private ZooKeeper zk;
	static String IP;
	static int port;
	static String playerName;

	

	public static void main(String[] args) {
		if(args.length > 1){
			String temp = args[0];
			String t[] = temp.split(":");
			if(t.length==2){
				port = Integer.parseInt(t[1]);
			} else {
				port = 6000;
			}
			IP = t[0];
			playerName = args[1];
			System.out.println("Player Name " + playerName + " IP "+ IP + " Port "+port);
			//Player player = new Player();
			String connectionString=IP+":"+port;
			try{
				zkConnection zkc = new zkConnection();
				ZooKeeper zk = zkc.connect(connectionString);

				if(zk.exists(BASE_PATH, false)==null){
					zkc.createNode(BASE_PATH, "Home Node for Abhash".getBytes());
				}

				if(zk.exists(BASE_PATH+"/"+playerName, false)!=null){
					System.out.println("Error: Player already exists!");
					System.exit(0);
				}
				Scanner in = new Scanner(System.in);
				int score = in.nextInt();
				while(true){
					//System.out.println(" Logs: Score read is "+score);
					if(zk.exists(BASE_PATH+"/"+playerName, false)==null){
						zkc.createNode(BASE_PATH+"/"+playerName,Integer.toString(score).getBytes());
					} else {
						//update the score
						zkc.updateNode(BASE_PATH+"/"+playerName, Integer.toString(score).getBytes());
					}
					score = in.nextInt();
				}
			} catch(Exception e){
				System.out.println("Some thing wrong happened!");
			}
		}
		else 
		{
			System.out.println("<usage>: player <ip>:port player_name");
		}
	}
}
