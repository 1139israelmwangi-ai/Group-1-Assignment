import java.util.*;


//Packet Class


class Packet {
    private int pktId;
    private String src;
    private String dst;
    private int sizeBytes;
    private long timestamp;
    private String currentRouter;

    public Packet(int pktId, String src, String dst, int sizeBytes, long timestamp){
        this.pktId = pktId;
        this.src = src;
        this.dst = dst;
        this.sizeBytes = sizeBytes;
        this.timestamp = timestamp;
        this.currentRouter = null;
    }

    public void setRouter( String routerId){
        this.currentRouter = routerId;
    }
    
    public int getPktId() { return pktId; }
    public String getSrc() { return src; }
    public String getDst() { return dst; }
    public int getSizeBytes() { return sizeBytes; }
    public long getTimestamp() { return timestamp; }
    public String getCurrentRouter() { return currentRouter; }

}

//RouterNode class

class RouterNode{
    private String nodeId;
    private Map<String, String> routingTable = new HashMap<>();
    private List<Packet> queue = new ArrayList<>();

    public RouterNode(String nodeId){
        this.nodeId = nodeId;
    }

    public void receivePacket(Packet pkt){
        pkt.setRouter(nodeId);
        queue.add(pkt);
    }

    public Packet sendPacket(RouterNode dstRouter){
        if (queue.isEmpty()) return null;

        Packet pkt = queue.remove(0);
        dstRouter.receivePacket(pkt);
        return pkt;
    }

    public void setRoute(String dst, String nextHop){
        routingTable.put(dst, nextHop);
    }

    public List<Packet> getQueue(){
        return queue;
    }

}

// BillingEngine class

class BillingEngine {
    private double ratePerMb;
    private List<Map<String, Object>> records = new ArrayList<>();

    public BillingEngine( double ratePerMb){
        this.ratePerMb = ratePerMb;
        
    }

    public void bill(Packet pkt) {
        double charge = (pkt.getSizeBytes() / (1024.0 * 1024.0)) * ratePerMb;

        Map<String, Object> record = new HashMap<>();
        
        record.put("packet", pkt.getPktId());
        record.put("src", pkt.getSrc());
        record.put("dst", pkt.getDst());
        record.put("router", pkt.getCurrentRouter());
        record.put("charge", charge);
        record.put("timestamp", pkt.getTimestamp());

        records.add(record);
    }
    
    public List<Map<String, Object>> getRecords() {
        return records;
    }

}

public class Testing {
    public static void main (String[] args){
        // Create some Router objects
        RouterNode R1 = new RouterNode("R1");
        RouterNode R2 = new RouterNode("R2");

        // Create a billing engine

        BillingEngine billingEngine = new BillingEngine(0.04);

        //Define routes

        R1.setRoute("H2", "R2");
        R2.setRoute("H1", "R1");

        // Define some Packets, two packets.
        Packet pkt1 = new Packet(1, "H1", "H2", 1200, System.currentTimeMillis());
        Packet pkt2 = new Packet(2, "H2", "H1", 1500, System.currentTimeMillis());

        // Multiplicity demonstration; R1 => many packets
        R1.receivePacket(pkt1);
        R1.receivePacket(pkt2);

        // Routing demonstration
        Packet sent1 = R1.sendPacket(R2);
        Packet sent2 = R1.sendPacket(R2);

        //Billing for packets at R2
        for ( Packet pkt : R2.getQueue()) {
            billingEngine.bill(pkt);
        }

        //Output billing summary
        System.out.println("\n==== BILLING RECORDS ===");
        System.out.println(billingEngine.getRecords());
    }
}