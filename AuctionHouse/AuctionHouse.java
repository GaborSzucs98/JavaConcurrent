import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

public class AuctionHouse {
    static class NFT {
        public final int artistIdx;
        public final int price;

        public NFT(int artistIdx, int price) {
            this.artistIdx = artistIdx;
            this.price = price;
        }
    }

    static class AuctionBid {
        public int bidPrice;
        public String collectorName;

        public AuctionBid(int bidPrice, String collectorName) {
            this.bidPrice = bidPrice;
            this.collectorName = collectorName;
        }
    }

    static int failCount = 0;

    static final int MAX_NFT_PRICE = 100;
    static final int MAX_NFT_IDX = 100_000;
    static final int MAX_COLLECTOR_BID = MAX_NFT_IDX / 100;

    private static final int COLLECTOR_MIN_SLEEP = 10;
    private static final int COLLECTOR_MAX_SLEEP = 20;
    private static final int MAX_AUCTION_BIDS = 10;

    static final int ARTIST_COUNT = 10;
    static final int COLLECTOR_COUNT = 5;

    static final int INIT_ASSETS = MAX_NFT_IDX / 10 * MAX_NFT_PRICE;

    static int nftIdx = 0;
    static int remainingNftPrice = INIT_ASSETS;
    static NFT[] nfts = new NFT[MAX_NFT_IDX];

    static int totalCommission = 0;
    static int noAuctionAvailableCount = 0;
    static int soldItemCount = 0;

    // TODO for Task 2: data structure "auctionQueue"
    static BlockingQueue <AuctionBid> auctionQueue = new LinkedBlockingQueue<>();

    // TODO for Task 3: data structure "owners"
    static List<String> owners= new ArrayList<String>();

    public static void main(String[] args) throws InterruptedException {
        // Task 1
        List<Thread> artists = makeArtists();

        // Task 2
        Thread auctioneer = makeAuctioneer(artists);

        // Task 3
        List<Thread> collectors = makeCollectors(auctioneer);

        // TODO make sure that everybody starts working
        // TODO make sure that everybody finishes working

        runChecks();
    }

    // ------------------------------------------------------------------------
    // Task 1

    private static List<Thread> makeArtists() {

        List<Thread> artist = new ArrayList<Thread>();
        for (int i=0;i<ARTIST_COUNT;i++){
            final int id=i;
            Thread firkász= new Thread(new Runnable(){
                @Override public void run(){
                    while(nftIdx<MAX_NFT_IDX){
                        int price = getRandomBetween(100,1000);
                        if (price > remainingNftPrice){
                            break;
                        }else{
                            NFT nft= new NFT(id,price);
                            nfts[nftIdx]=nft;
                            nftIdx++;
                            remainingNftPrice=remainingNftPrice-price;
                        }
                        sleepForMsec(20);
                    }
                }
            }
            );
            firkász.start();
            artist.add(firkász);
        }
        return artist;

        // TODO create ARTIST_COUNT artists as threads, all of whom do the following, and return them as a list

        // every 20 milliseconds, try to create an NFT in the following way
            // the artist chooses a price for the nft between 100 and 1000
            // if the nfts array is already fully filled, the artist is done
            // if the price is more than remainingNftPrice, the artist is done
            // the artist creates the NFT in the next nfts array slot
            // ... and deduces the price from remainingNftPrice
    }

    // ------------------------------------------------------------------------
    // Task 2

    private static Thread makeAuctioneer(List<Thread> artists) {

        Thread auctioneer = new Thread(new Runnable(){
            @Override public void run(){
                
                NFT nft=null;
                boolean working=true;
                boolean overwork=false;
                boolean stillwork;
                int remain=100;
                int bids=0;
                int highest=0;
                AuctionBid current=null;
                AuctionBid currenthighest;

                while(working || overwork){
                    if(nftIdx>0 && !auctionQueue.isEmpty()){
                    nft=nfts[nftIdx];
                    nftIdx=nftIdx-1;
                    while(bids<MAX_AUCTION_BIDS && !auctionQueue.isEmpty()){
                        current=auctionQueue.remove();
                        if(current.bidPrice>highest){
                            currenthighest=current;
                            highest=currenthighest.bidPrice;
                        }
                        sleepForMsec(1);
                    }
                
                
                soldItemCount=soldItemCount+1;
                int commission=(int)((nft.price+highest)*0.1);
                totalCommission=totalCommission+commission;
                if(!owners.contains(current.collectorName)){
                    owners.add(current.collectorName);
                }

                sleepForMsec(3);

            }
                    
                
                    stillwork=false;
                    for(Thread thread : artists){
                        stillwork = stillwork || thread.isAlive();
                    }
                    if(!stillwork){
                        working=false;
                        overwork=true;
                    }
                    if(overwork){
                        remain=remain-1;
                    }
                    if(remain==0){
                        overwork=false;
                    }
                }
            }
        }
        );
        auctioneer.start();
        return auctioneer;


        // TODO create and return the auctioneer thread that does the following

        // run an auction if 1. any artists are still working 2. run 100 auctions after all artists are finished
            // otherwise, the auctioneer is done
        // a single auction is done like this:
            // pick a random NFT from nfts (keep in mind that nfts can still be empty)
            // create the auctionQueue
            // wait for auction bids
                // if there were already MAX_AUCTION_BIDS, the auction is done
                // if no bid is made within 1 millisecond, the auction is done
                // only for Task 3: if a bid is made and it has a better price than all previous ones, this is the currently winning bid
            // once the auction is done, add the commission to totalCommission
                // the commission is 10% of the price of the NFT (including the sum in the winning bid, if there was any)
                // only for Task 3: if there was a bid, increase soldItemCount and remember that the collector owns an NFT
            // now set auctionQueue to null and keep it like that for 3 milliseconds
        }

    // ------------------------------------------------------------------------
    // Task 3

    private static List<Thread> makeCollectors(Thread auctioneer) {
        
        List<Thread> collectors= new ArrayList<Thread>();
        return collectors;
        // TODO create collectors now, the collectors' names are simply Collector1, Collector2, ...

        // work until the auctioneer is done (it is not isAlive() anymore)
            // sleep for COLLECTOR_MIN_SLEEP..COLLECTOR_MAX_SLEEP milliseconds randomly between each step
        // if there is no auction available, just increase noAuctionAvailableCount
        // if there is an ongoing auction, and you haven't bid on it already, place a bid
            // choose your bid between 1..MAX_COLLECTOR_BID randomly
    }

    // ------------------------------------------------------------------------
    // Tester

    private static String isOK(boolean condition) {
        if (!condition)   ++failCount;
        return isOkTxt(condition);
    }

    private static String isOkTxt(boolean condition) {
        return condition ? "GOOD" : "BAD ";
    }

    private static void runChecks() {
        if (Thread.activeCount() == 1) {
            System.out.printf("%s Only the current thread is running%n", isOK(true));
        } else {
            System.out.printf("%s %d threads are active, there should be only one%n", isOK(Thread.activeCount() == 1), Thread.activeCount());
        }

        System.out.printf("%s nftIdx > 0%n", isOK(nftIdx > 0));

        int soldPrice = IntStream.range(0, nftIdx).map(idx-> nfts[idx].price).sum();
        System.out.printf("%s Money is not lost: %d + %d = %d%n", isOK(soldPrice + remainingNftPrice == INIT_ASSETS), soldPrice, remainingNftPrice, INIT_ASSETS);

        System.out.printf("%s [Only Task 2] Total commission not zero: %d > 0%n", isOK(totalCommission > 0), totalCommission, INIT_ASSETS);

        System.out.printf("%s [Only Task 3] Sold item count not zero: %d > 0%n", isOK(soldItemCount > 0), soldItemCount, INIT_ASSETS);
        System.out.printf("%s [Only Task 3] Some collectors have become owners of NFTs: %d > 0%n", isOK(owners.size() > 0), owners.size(), INIT_ASSETS);
        System.out.printf("%s [Only Task 3] Sometimes, collectors found no auction: %d > 0%n", isOK(noAuctionAvailableCount > 0), noAuctionAvailableCount, INIT_ASSETS);

        System.out.printf("%s Altogether %d condition%s failed%n", isOkTxt(failCount == 0), failCount, failCount == 1 ? "" : "s");

        // forcibly shutting down the program (don't YOU ever do this)
        System.exit(42);
    }

    // ------------------------------------------------------------------------
    // Utilities

    private static int getRandomBetween(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max+1);
    }

    private static void sleepForMsec(int msec) {
        try {
            Thread.sleep(msec);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
