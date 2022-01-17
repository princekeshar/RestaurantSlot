
#Tables
<br>FK  - Foreign Key  </br>
<br>PK - Primary Key  </br>

<h4>1. Users </h4>
<p>  User table, which stores basic info of users and most importantly userId which will be used in all tables. 
</p>
               
`    userid, email, firstname, lastname
`      
<h4>2. CentralOrderTable </h4>

Tables contains data of all types of orders future, recurring and single time.

        ` OrderId (PK), CrossTableId,  meals, OrderTime, updatedTime, firstInsertTime, Status
         
         OrderID: It will contains all orders processed or need to process by system. 
         CrossTableId : it can be future Id (FutXXX)  or RecurId  (RECXXXX).
         Meals : comma separated string or json, ( Depends on max size we allow ) 
         OrderTime: time at which process need to be scheduled. 
         Status: Active, Processing, Processed, Denied. If value is Active, order need to process.
`Table should be partitioned by OrderTime, Year, Month, Date. so that it’s easy to fetch recent data. 
Further if we have too many orders in a single we can partitions in hours, for ex:  4 partition per day

<h4>3. RecurringOrder </h4>
Table contains meals defined by user.

           ` RecurId (PK), UserId, Active, OrderTime, meals, userId, updatedTime,  firstInsertTime, DisabledDates
            Active : It’s boolean flag, if user want to disable or enable. 
            DisabledDates : If user want to disable recurring order for given range of dates. It can contains comma separated regex.
`
  Table is partitioned by Year, month, date ( and hour if we have too many data in one day ).  
  Here by default recur time is daily, 

<h4>4. RecurringHistoryTable </h4>
This is used only for history analysis, if user change any thing in defined recurring table,
we first insert original data into history and then update data in recurring Order.


`        HistoryId (PK), RecurId (FK with RecurringOrder table), UserId, Active, OrderTime, Meals, UserId, updatedTime, insertTime
`

<h4>5. FutureTable </h4>

This table is similar to recurring table, it contains orders which is non recurring and supposed to take place in future time. 

           ` FutureId (PK), ScheduledTime, Meals, UserId, Status , updatedTime, insertTime
            Status #(cancelled, active, processed )
            Any single time future order will be saved in this table. 
            Here primary key is futureId. 
            Table is partition by Year, Month, Date. ( and hours if needed).
`
<h4>6. FutureHistoryTable </h4>
This table is similar to RecurringHistoryTable

`          Historyid, FutureId (FK with FutureTable), Meals, UserId, Time, Status, updatedTime, insertTime
`
# Scenarios


<h4>Scenario 1</h>

<li> On Demand Order - Entry will be added in central table with status processing and order will be sent to queue. Once order is processed, status will be updated as Processed/Denied </li>

<li> Future Order: Future Order will be saved in Future table. If user update future order, we will add entry in history table and update future table. 
To process, we will have a scheduler, which will run and poll db in every 30 minutes and it will fetch all Active orders which needs to process at that time from FutureTable table, it will add entry in CentralOrderTable as processing and these orders will be sent to queue for processing. FutureTable status will be marked Processed immediately i.e. so that scheduler don't pick order again.
       User will not be able to update if scheduler has picked up Order i.e. FutureTable status is Processed.
           </li>
<li> Recurring Order: Scheduler will work similar to future order for recurring. For recurring order, we will check whether it's active or not, if user has marked dates as disabled or not. Using regex and comma, we can save date ranges and commas.  </li>
<br> </br>
<br>Here issue is with this approach, if we have load in system future or recurring order might not be processed  ( deliver ) to make sure all future/recur orders get delivered: </br>
<li> Before saving order to database we can check if restaurant can accommodate order or not i.e. from coding question, if restaurant has 7 slots, it can't prepare 4 main course as each main course will require 2 slots.  </li>
<li> We can put future and recurring order in fast priority queue so that they will get preference over any on demand order.</li>
<li> To process future/recurring orders on time, we can run another scheduler which will keep checking load on system due to current order and pick future and recurring order early. For recurring order, we will need extra flag so that scheduler don'n pick again for same period. </li>