
public class ServerTable{
    public final ServerInfo[] serverList;
        
        public class ServerInfo{
            public final String hostAddress;
            public final int portNum;

            public ServerInfo(String hostPort){
                String[] hostPortArray = hostPort.split(":");
                this.hostAddress = hostPortArray[0];
                this.portNum = Integer.parseInt(hostPortArray[1]);
            }
        }
        
        public ServerTable(int numOfServers, String[] arrayOfServerInfo){
            this.serverList = new ServerInfo[numOfServers];
            for(int i = 0; i < numOfServers; i++){
                this.serverList[i] = new ServerInfo(arrayOfServerInfo[i]);
            }
        }
    }
