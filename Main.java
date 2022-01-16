
import java.text.DecimalFormat;
import  java.util.*;
//import com.google.gson.Gson;
/**
  Program takes input function from prepareInput method, to parrse data in json format import GSon library. 
  <dependency>
            <groupId>com.google.code.gson</groupId>
            <artifactId>gson</artifactId>
            <version>2.8.9</version>
        </dependency>
*/

class Main {
  public static final Integer MAX_RESTAURANT_SLOT = 7 ;
    public static final Integer MAIN_COURSE_SLOT = 2 ;
    public static final Integer APPETIZER_SLOT = 1 ;

    public static final Integer APPETIZER_PREP_TIME = 17 ;
    public static final Integer MAIN_COURSE_PREP_TIME = 29 ;
    public static final Integer TIME_TO_TRAVEL_PERKM = 8 ;

    public static final Double THRESHOLD_TIME = 150.0;
    public  static  DecimalFormat decimalFormat = new DecimalFormat("0.#");
    public  static  PriorityQueue<Task> queue = new PriorityQueue<>();

    public static Double startTime = 0.0;
    
    public static void main(String[] args) {
        Order[] orders = prepareInput();
        processOrder(orders);

    }

    static Order[] prepareInput(){
      /*  String jsonString = "[{\"orderId\": 12, \"meals\": [\"A\", \"A\"], \"distance\": 5},\n" +
                "{\"orderId\": 21, \"meals\": [\"A\", \"M\"], \"distance\": 1},\n" +
                "{\"orderId\": 14, \"meals\": [\"M\", \"M\", \"M\", \"M\", \"A\", \"A\", \"A\"], \"distance\": 10},\n" +
                "{\"orderId\": 32, \"meals\": [\"M\"], \"distance\": 0.1},\n" +
                "{\"orderId\": 22, \"meals\": [\"A\"], \"distance\": 3}]";
       Gson gson = new Gson();
      //  Order [] orders=gson.fromJson(jsonString,Order[].class); */

        Order[] orders = new Order[5];

        orders[0] = new Order( 12, new String[]{"A", "A"}, 5.0);
        orders[1] = new Order( 21, new String[]{"A", "M"},1.0);
        orders[2] = new Order( 14, new String[]{"M", "M", "M", "M", "A", "A"},1.0 );
        orders[3] = new Order( 32, new String[]{"M"},0.1 );
        orders[4] = new Order( 22, new String[]{"A"},3.0 );
        return  orders;
    }

    static class Task implements  Comparable<Task>{

        Double time;
        Integer orderId;

        public  Task(Double time, Integer orderId){
            this.time = time;
            this.orderId = orderId;
        }

        @Override
        public int compareTo(Task o) {
            return  this.time.compareTo(o.time);
        }
    }
    
    

    public  static  void processOrder(Order[] orders){
        
        if(orders==null || orders.length ==0){
            return;
        }

        for(Order  order :orders){
            ItemCount itemCount = order.getItemCount();
            int requiredSlots = itemCount.totalSlots();
            if(requiredSlots > MAX_RESTAURANT_SLOT){
                System.out.println(String.format("Order %s is denied because the restaurant cannot accommodate it.", order.orderId));
                continue;
            }

            ArrayList<Task>  tempList = new ArrayList<>();
            Double waitingTime = Double.MIN_VALUE;
            while (  (MAX_RESTAURANT_SLOT - queue.size()) < requiredSlots){
                Integer orderId = queue.peek().orderId;
                waitingTime = Math.max ( waitingTime,  queue.peek().time ) ;
                while ( queue!=null && queue.size() >0 &&  orderId.equals(queue.peek().orderId) ){
                    tempList.add(queue.poll());
                }
            }


            Double deliveryTime  = order.distance * TIME_TO_TRAVEL_PERKM;
            int prepTime =  itemCount.noOfMainCourse > 0 ? MAIN_COURSE_PREP_TIME  : APPETIZER_PREP_TIME;
            Double totalPrepTime = deliveryTime + prepTime + waitingTime + startTime;
            if(totalPrepTime > THRESHOLD_TIME){
                System.out.println(String.format("Order %s is denied because the restaurant cannot accommodate it.", order.orderId));
                for(Task task : tempList){
                    queue.add(task);
                }
                continue;
            }

            startTime = startTime + waitingTime;

            System.out.println(String.format( "Order %s will get delivered in %s minutes", order.orderId, decimalFormat.format(totalPrepTime) ));
            for( int k=0; k< requiredSlots ; k++){
                queue.add( new Task( totalPrepTime, order.orderId));
            }
        }

    }

    static  class  ItemCount{

        int noOfAppetizer = 0;
        int noOfMainCourse = 0;

        public ItemCount(int noOfAppetizer, int noOfMainCourse){
            this.noOfAppetizer = noOfAppetizer;
            this.noOfMainCourse = noOfMainCourse;
        }

        public  int totalSlots(){
            return  (MAIN_COURSE_SLOT * this.noOfMainCourse) +  ( APPETIZER_SLOT * this.noOfAppetizer);
        }
    }

    static class Order {
        Integer  orderId;
        String[] meals;
        Double distance;


        public Order(Integer  orderId, String[] meals, Double distance){
            this.orderId= orderId;
            this.meals = meals;
            this.distance = distance;
        }

        public ItemCount getItemCount(){

            int noOfAppetizer = 0;
            int noOfMainCourse = 0;
            for ( String s  :this.meals ){
                if("A".equals(s)){
                    noOfAppetizer++;
                }else if ( "M".equals(s)){
                    noOfMainCourse++;
                }else{
                    throw new RuntimeException("Not Valid Menu Item");
                }
            }
            return  new ItemCount(noOfAppetizer, noOfMainCourse);
        }

    }
    
}