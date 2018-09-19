import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.Watcher.Event.KeeperState;

class PlayerInfo{
	private String playerName;
	private int score;
	private boolean isOnline;
	private long mTime;
	public PlayerInfo(String name,int score,boolean isonline,long time) {
		this.playerName = name;
		this.score = score;
		this.isOnline = isonline;
		this.mTime = time;
	}
	
	public static Comparator<PlayerInfo> COMPARE_BY_TIME = new Comparator<PlayerInfo>() {
		public int compare(PlayerInfo one, PlayerInfo other){
			return one.mTime > other.mTime ? -1: one.mTime < other.mTime ?1:0; 
		}
	}; 
	public static Comparator<PlayerInfo> COMPARE_BY_SCORE = new Comparator<PlayerInfo>() {
		public int compare(PlayerInfo one,PlayerInfo other){
			return one.score > other.score ? -1 : one.score < other.score ?1:0;
		}
	};
	public String getPlayerName() {
		return playerName;
	}
	public void setPlayerName(String playerName) {
		this.playerName = playerName;
	}
	public int getScore() {
		return score;
	}
	public void setScore(int score) {
		this.score = score;
	}
	public boolean isOnline() {
		return isOnline;
	}
	public void setOnline(boolean isOnline) {
		this.isOnline = isOnline;
	}
	public long getmTime() {
		return mTime;
	}
	public void setmTime(long mTime) {
		this.mTime = mTime;
	}
}


public class viewer {
	public static final String BASE_PATH = "/ajain28";
	public static final String SCORE_PATH = BASE_PATH + "/scores";
	public static final String ONLINE_PATH = BASE_PATH + "/online";
	
	static String IP;
	static int port;
	static int N;
	
	private ZooKeeper zk;
	private CountDownLatch connSignal = new CountDownLatch(0);
	
	static ArrayList<PlayerInfo> recentScores = new ArrayList<PlayerInfo>();
	static ArrayList<PlayerInfo> highestScores = new ArrayList<PlayerInfo>();
	static  ArrayList<PlayerInfo> newList = new ArrayList<PlayerInfo>();
	
	public ZooKeeper connect(String host) throws Exception {
		zk = new ZooKeeper(host, 3000, new Watcher() {
			@SuppressWarnings("unchecked")
			public void process(WatchedEvent event) {
				String eventPath = event.getPath();
				if (event.getState() == KeeperState.SyncConnected) {
					System.out.println("CLient is sync!");
					connSignal.countDown();
				} else if(event.getType()==EventType.NodeChildrenChanged){
					System.out.println("Node value is updated");
				}
				if(event.getType() == Event.EventType.None) {
					if(event.getState() == KeeperState.Disconnected) {
						System.out.println("Node is disconnected!");
					}
				}
				//if(eventPath!=null) {
					System.out.println("Watcher has received the event "+eventPath);
					try {
						List<String> childs = zk.getChildren(SCORE_PATH, false);
						newList.clear();
						for(String s:childs) {
							String t[] = s.split(":");
							PlayerInfo pl = new PlayerInfo(t[0], Integer.parseInt(t[1]), false, Integer.parseInt(t[2]));
							newList.add(pl);
						}
					} catch(Exception e) {
						System.out.println("Catched in process!");
						e.printStackTrace();
					}
					recentScores.clear();
					highestScores.clear();
					recentScores = (ArrayList<PlayerInfo>)newList.clone();
					highestScores = (ArrayList<PlayerInfo>)newList.clone();
					Collections.sort(recentScores,PlayerInfo.COMPARE_BY_TIME);
					Collections.sort(highestScores, PlayerInfo.COMPARE_BY_SCORE);
					if(recentScores.size()>N) {
						ArrayList<PlayerInfo> r1 = new ArrayList<PlayerInfo>(recentScores.subList(0, N));
						recentScores = r1;
					}
					if(highestScores.size()>N) {
						ArrayList<PlayerInfo> h1 = new ArrayList<PlayerInfo>(highestScores.subList(0, N));
						highestScores = h1;
					}
					//Set the flag for online by visiting the node in ONLINE_PATH
					List<String> onlineNodes = new ArrayList<String>();
					try{
						onlineNodes = zk.getChildren(ONLINE_PATH, false);
					} catch(Exception e) {
						System.out.println("Error: Online Node children fails !");
						e.printStackTrace();
					}
					for(String s:onlineNodes) {
						for(PlayerInfo p:recentScores) {
							if(p.getPlayerName().equals(s)) {
								p.setOnline(true);
							}
						}
					}
					for(String s:onlineNodes) {
						for(PlayerInfo p :highestScores) {
							if(p.getPlayerName().equals(s)) {
								p.setOnline(true);
							}
						}
					}
					System.out.println("Most recent scores");
					System.out.println("------------------");
					for(PlayerInfo p :recentScores){
						String online = p.isOnline() ? " **" : "";
						System.out.println(p.getPlayerName() + "\t" + p.getScore() +online);
					}
					System.out.println("Highest scores");
					System.out.println("--------------");
					for(PlayerInfo p : highestScores){
						String online  = p.isOnline() ? " **":"";
						System.out.println(p.getPlayerName() + "\t" + p.getScore() + online);			
					}
					
				//}
			}
		});
		connSignal.await();
		return zk;
	}
	public static void main(String[] args) {
		if(args.length==2) {
			String temp = args[0];
			String t[] = temp.split(":");
			IP = t[0];
			if(t.length == 2){
				port = Integer.parseInt(t[1]);
			} else {
				port = 6000;
			}
			N = Integer.parseInt(args[1]);
			String connectionString = IP +":"+port;
			viewer view = new viewer();
			try {
				ZooKeeper zk = view.connect(connectionString);
				if(zk==null) {
					System.out.println("Error:Connection failed to zookeeper!");
					System.exit(0);
				}
				while(true) {
					List<String> chd = zk.getChildren(SCORE_PATH, true);
					List<String> chd1 = zk.getChildren(ONLINE_PATH, true);
				}
			} catch (Exception e) {
				System.out.println("Error: Something went wrong in watcher!");
				e.printStackTrace();
			}
		}else {
			System.out.println("Usage: <exe name> <IP[:port]> N");
			System.exit(0);
		}//end of If for args check
	}
}
