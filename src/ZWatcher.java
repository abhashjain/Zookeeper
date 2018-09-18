

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.data.Stat;

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
			return one.mTime < other.mTime ? -1: one.mTime > other.mTime ?1:0; 
		}
	}; 
	public static Comparator<PlayerInfo> COMPARE_BY_SCORE = new Comparator<PlayerInfo>() {
		public int compare(PlayerInfo one,PlayerInfo other){
			return one.score < other.score ? -1 : one.score >other.score ?1:0;
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

public class ZWatcher {
	public static final String BASE_PATH = "/ajain28";
	static String IP;
	static int N;
	static int port;
	private ZooKeeper zk;
	private CountDownLatch connSignal  = new CountDownLatch(0);
	static ArrayList<PlayerInfo> recentScores = new ArrayList<PlayerInfo>();
	static  ArrayList<PlayerInfo> highestScores = new ArrayList<PlayerInfo>();
	private ArrayList<PlayerInfo> newList;
	
	public ZooKeeper connect(String host) throws Exception {
		newList = new ArrayList<PlayerInfo>();
		zk = new ZooKeeper(host, 3000, new Watcher() {
			public void process(WatchedEvent event) {
				String path = event.getPath();
				if (event.getState() == KeeperState.SyncConnected) {
					connSignal.countDown();
					System.out.println("Node is Connected for "+path);
				}else if(event.getState()==KeeperState.Disconnected){
					System.out.println("Node got disconnected for "+path);
				}
				
				if(event.getType() == Event.EventType.None){
					if(event.getState()==KeeperState.SyncConnected){
						System.out.println("Connected!" +path);
					} else if(event.getState()==KeeperState.Expired){
						//Mark node as offline
						System.out.println("Expired!"+path);
					}
				} else {
					if(path!=null){
						System.out.println("Watcher Event received for "+path);
						//get the list of new nodes
						try{
							List<String> childs = zk.getChildren(BASE_PATH, false);
							System.out.println("Log1");
							for(String c :childs){
								//fill the temp list with latest player info
								Stat stat = new Stat();
								byte b[] = zk.getData(BASE_PATH+"/"+c, false, stat);
								System.out.println("INfo for "+c+ " "+ new String(b,"UTF-8") + " " + stat.getMtime());
								PlayerInfo pi = new PlayerInfo(c, Integer.parseInt(new String(b,"UTF-8")), true, stat.getMtime());
								newList.add(pi);
							}
						}catch(Exception e){
							System.out.println("Exception caught!");
						}
						System.out.println("Log2");
						//check the size of most recent scores
						if(recentScores.size()==0 && highestScores.size()==0){
							recentScores = newList;
							highestScores = newList;
						} else {
							//there are some elements are already present
							for(PlayerInfo p : recentScores){
								p.setOnline(false);
							}
							for(PlayerInfo p : highestScores){
								p.setOnline(false);
							}
							for(PlayerInfo p : newList){
								for(PlayerInfo q : recentScores ){		
									if(p.getPlayerName().equals(q.getPlayerName()) && p.getScore()==q.getScore()){
										q.setOnline(true);
									} else if(p.getPlayerName().equals(q.getPlayerName())){
										q.setOnline(true);
									} else {
										recentScores.add(p);
									}
								}
							}
							for(PlayerInfo p : newList){
								for(PlayerInfo q : highestScores ){
									if(p.getPlayerName().equals(q.getPlayerName()) && p.getScore()==q.getScore()){
										q.setOnline(true);
									} else if(p.getPlayerName().equals(q.getPlayerName())){
										q.setOnline(true);
									} else {
										highestScores.add(p);
									}
								}
							}//end of for
						}//end of else
						//print the Most recent scores
						Collections.sort(recentScores, PlayerInfo.COMPARE_BY_TIME);
						Collections.sort(highestScores, PlayerInfo.COMPARE_BY_SCORE);
						if(recentScores.size()>N){
							ArrayList<PlayerInfo> r1 = new ArrayList<PlayerInfo>(recentScores.subList(0, N));
							recentScores = r1;
						}
						if(highestScores.size()>N){
							ArrayList<PlayerInfo> h1 = new ArrayList<PlayerInfo>(highestScores.subList(0, N));
							highestScores = h1;
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
					}
				}}
		});
		connSignal.await();
		return zk;
	}
	public void close() throws InterruptedException {
		zk.close();
	}
	
public static void main(String[] args) {
	if(args.length > 0 ){
		String temp = args[0];
		String t[] = temp.split(":");
		IP = t[0];
		if(t.length == 2){
			port = Integer.parseInt(t[1]);
		} else {
			port = 6000;
		}
		N = Integer.parseInt(args[1]);
		//System.out.println("IP is "+ IP + " port is " + port + " N is " + N);
		ZWatcher zkc = new ZWatcher();
		String connectionString = IP+":"+port;
		
		try{
			ZooKeeper zk = zkc.connect(connectionString);
			if(zk.exists(BASE_PATH, false)==null){
				System.out.println("No DB exist!");
			}
			while(true){
				List<String> chd = zk.getChildren(BASE_PATH, true);
				for(String child: chd){
					zk.exists(BASE_PATH+"/"+child, true);
				}
			}
		} catch(Exception e){
			System.out.println("Some error has occured!");
		}
		
	} else {
		System.out.println("Usage: watcher IP:port N");
	}
}
}
