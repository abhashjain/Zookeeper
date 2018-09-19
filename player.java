import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;


import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;



public class player {
	public static final String BASE_PATH = "/ajain28";
	public static final String SCORE_PATH = BASE_PATH + "/scores";
	public static final String ONLINE_PATH = BASE_PATH + "/online";
	public static final int DEVIATION = 1000;
	static String IP;
	static int port;
	static String playerName;
	
	private ZooKeeper zk;
	private CountDownLatch connSignal = new CountDownLatch(0);
	
	public ZooKeeper connect(String host) throws Exception {
		zk = new ZooKeeper(host, 3000, new Watcher() {
			public void process(WatchedEvent event) {
				if (event.getState() == KeeperState.SyncConnected) {
					//System.out.println("CLient is sync!");
					connSignal.countDown();
				} else if(event.getType()==EventType.NodeChildrenChanged){
					//System.out.println("Node value is updated");
				}
			}
		});
		connSignal.await();
		return zk;
	}
	
	public void close() throws InterruptedException {
		zk.close();
	}

	public void persis_createNode(String path, byte[] data) throws Exception
	{
		zk.create(path, data, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
	}
	public void ep_createNode(String path, byte[] data) throws Exception
	{
		zk.create(path, data, Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
	}

	public void seq_createNode(String path, byte[] data) throws Exception
	{
		zk.create(path, data, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT_SEQUENTIAL);
	}
	
	public void updateNode(String path, byte[] data) throws Exception
	{
		zk.setData(path, data, zk.exists(path, true).getVersion());
	}

	public void deleteNode(String path) throws Exception
	{
		zk.delete(path,  zk.exists(path, true).getVersion());
	}
	public static ZooKeeper init(player p1,String host) throws Exception{
		ZooKeeper zk = p1.connect(host);
		if(zk.exists(BASE_PATH, false)==null) {
			p1.persis_createNode(host, "Base Node".getBytes());
		}
		if(zk.exists(SCORE_PATH, false)==null) {
			p1.persis_createNode(SCORE_PATH, "SCORES".getBytes());
		}
		if(zk.exists(ONLINE_PATH, false)==null) {
			p1.persis_createNode(ONLINE_PATH, "IS ONLINE PARENT".getBytes());
		}
		return zk;
	}
	
	public static void main(String[] args) {
		if(args.length<2) {
			System.out.println("Usage:<class> <name> [count] [delay] [score]");
			System.exit(0);
		} else if(args.length==2) {
			String temp = args[0];
			String t[] = temp.split(":");
			if(t.length==2) {
				port = Integer.parseInt(t[1]);
			} else {
				port = 6000;
			}
			IP = t[0];
			playerName = args[1];
			//System.out.println("Player Name " + playerName + " IP "+ IP + " Port "+port);
			String connectionString = IP + ":" + port;
			player player = new player();
			try {
				ZooKeeper zk = init(player,connectionString);
				if(zk==null) {
					System.out.println("Error: Zookeeper is not connected!");
					System.exit(0);
				}
				//check the online node and create the node 
				if(zk.exists(ONLINE_PATH + "/" + playerName, false)==null) {
					player.ep_createNode(ONLINE_PATH + "/"+playerName, "Player online".getBytes());
				} else {
					System.out.println("Error: Player already exists!");
					System.exit(0);
				}
				Scanner in = new Scanner(System.in);
				System.out.println("Enter a score or 'n' to exit!");
				String input = in.next();
				if(input.equals("n") || input.equals("N")) {
					zk.close();
					System.exit(0);
				}
				int score =Integer.parseInt(input);
				//int score = in.nextInt();
				while(true) {
					//create a sequential node in format ONLINE_PATH:SCORE:seq_number
					player.seq_createNode(SCORE_PATH +"/" + playerName + ":" +score +":", "Scores".getBytes());
					System.out.println("Enter a score or 'n' to exit!");
					input = in.next();
					if(input.equals("n") || input.equals("N")) {
						zk.close();
						System.exit(0);
					}
					score =Integer.parseInt(input);
				}
			} catch (Exception e) {
				// TODO: handle exception
				System.out.println("Error: Player Catch Block!");
				e.printStackTrace();
			}
		} else if(args.length ==5){ //Normal Distribution case
			String temp = args[0];
			String t[] = temp.split(":");
			if(t.length==2) {
				port = Integer.parseInt(t[1]);
			} else {
				port = 6000;
			}
			IP = t[0];
			playerName = args[1];
			int count = Integer.parseInt(args[2]);
			float delay = Float.parseFloat(args[3]);
			int meanScore = Integer.parseInt(args[4]);
			String connectionString = IP + ":" + port;
			//System.out.println("Player Name " + playerName + " IP "+ IP + " Port "+port + " count " +count + " Mean Score "+meanScore + " delay " + delay);
			player player = new player();
			try {
				ZooKeeper zk = init(player,connectionString);
				if(zk==null) {
					System.out.println("Error: Zookeeper is not connected!");
					System.exit(0);
				}
				if(zk.exists(ONLINE_PATH + "/" + playerName, false)==null) {
					player.ep_createNode(ONLINE_PATH + "/"+playerName, "Player online".getBytes());
				} else {
					System.out.println("Error: Player already exists!");
					System.exit(0);
				}
				while(count>0) {
					int score = ((int)new Random().nextGaussian()*DEVIATION) + meanScore;
					player.seq_createNode(SCORE_PATH +"/" + playerName + ":" +score +":", "Scores".getBytes());
					Thread.sleep(((int)delay*1000));
					count--;
				}
			} catch (Exception e) {
				System.out.println("Distribution: Catch Block!");
				e.printStackTrace();
			}
		}
	} //End of main fn
}
