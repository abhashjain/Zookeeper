import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooDefs.Ids;


public class zkConnection {
	private ZooKeeper zk;
	private CountDownLatch connSignal  = new CountDownLatch(0);
	
	public ZooKeeper connect(String host) throws Exception {
		zk = new ZooKeeper(host, 3000, new Watcher() {
			public void process(WatchedEvent event) {
				if (event.getState() == KeeperState.SyncConnected) {
					System.out.println("CLient is sync!");
					connSignal.countDown();
				} else if(event.getType()==EventType.NodeChildrenChanged){
					System.out.println("Node value is updated");
				}
			}
		});
		connSignal.await();
		return zk;
	}
	public void close() throws InterruptedException {
		zk.close();
	}

	public void createNode(String path, byte[] data) throws Exception
	{
		zk.create(path, data, Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
	}

	public void updateNode(String path, byte[] data) throws Exception
	{
		zk.setData(path, data, zk.exists(path, true).getVersion());
	}

	public void deleteNode(String path) throws Exception
	{
		zk.delete(path,  zk.exists(path, true).getVersion());
	}
	
}
