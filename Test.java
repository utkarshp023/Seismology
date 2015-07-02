import java.io.*;
import java.util.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.io.FileWriter;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import regression.LinearRegression;

public class Test{
	public static double[] time;
	public static double[] rms;
	public static double[] k_values;
	public static double[] lhs;
	public static double b_time;
	public static double s_time;
	public static int arr_size;
	public static int stop;
	 
	public static void main(String args[]) throws IOException{
		//Scan the pitsa file 
		 Scanner s = null;
		 //Create an array to store the values
		 List<Integer> integers = new ArrayList<Integer>();

        try {
        	//Read the file
            s = new Scanner(new BufferedReader(new FileReader("pitsa001mahe")));

            while (s.hasNext()) {
                 if (s.hasNextInt()) {
                 		//Store the values in the arrayList
				        integers.add(s.nextInt());
				    } else {
				        s.next();
				    }
            }
        } finally {
            if (s != null) {
                s.close();
            }
        }
        Test ob = new Test();
        //Create a new file having all the required vaues only, the coda wave data points
        ob.createNewFile(integers);
        //Call linear regression to generate the slope
         LinearRegression lr = new LinearRegression(time, lhs);
         double slp = lr.slope();
         System.out.println("Slope :" + slp);
         Scanner in1 = new Scanner(System.in);
         //Ask user to enter the frquency on which the data is provided(the pitsa file)
         System.out.println("Enter the frequency(in Hz):");
    	 double fr = in1.nextDouble();
         //call to calculate Qc
         ob.QCalc(slp,fr);
	}

	public void createNewFile(List<Integer> integers){
		try
            {
            	//Called in the main method
            	Test t = new Test();
            	//Calculate the starting point of the coda wave
            	double skip = t.startingPointCalc() * 20;
        		//File that stores all the data values of arraylist
                File fac = new File("outputs.txt");
                if (!fac.exists())
                {
                    fac.createNewFile();
                }
                System.out.println("\n----------------------------------");
                System.out.println("The file has been created.");
                System.out.println("------------------------------------");
                
                FileWriter wr = new FileWriter(fac);
                Iterator iterator = integers.iterator();
                int count = 0;
                while(iterator.hasNext())
                {
                	//Skip the initial values as calculated by startingPointCalc()
                	if(count < skip + 6 + 112){
                		iterator.next();
                		iterator.remove();
                		count=count+1;
                		continue;
                	}
                	//write these values in the output.txt file
                    wr.write(iterator.next()+System.getProperty( "line.separator" ));
                }               
                wr.close();
                arr_size = integers.size();
                System.out.println("Size :" + arr_size);
                //call to calculate the number of windows and their size
                int windows = t.calcSize(b_time,s_time,skip/20);
                //call to calculate rms values
                t.rmsCalc(integers, windows);
                //call to calculate the respective time values
                t.timeCalc(skip/20);
                //call to calculate the k(x) values
                t.kCalc(skip/20);
                //call to calculate the lhs value i.e. ln[rms/k(x)]
                t.lhsCalc();

            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

	}

	public double startingPointCalc(){
		// calculates the initial point of coda wave generation based on the values of time fed by the user, 
		//in command line.
		int hhmm_o,hhmm_a,diff,sec,hhmm_i,at,ot,it;
    	double ssss_o,ssss_a,secf,diffs,ssss_i,atf,otf,itf,cd;
		
		Scanner in = new Scanner(System.in);
		
    	System.out.println("Enter the origin time :");
    	hhmm_o = in.nextInt();
		ssss_o = in.nextDouble();
		System.out.println("Hours :" +hhmm_o + " Sec :" + ssss_o);

		System.out.println("Enter the arrival time :");
    	hhmm_a = in.nextInt();
		ssss_a = in.nextDouble();
		System.out.println("Hours :" +hhmm_a + " Sec :" + ssss_a);
		
		System.out.println("Enter the instrument time :");
    	hhmm_i = in.nextInt();
		ssss_i = in.nextDouble();
		System.out.println("Hours :" +hhmm_i + " Sec :" + ssss_i);
		
		System.out.println("Enter the Bigger time window:");
    	b_time = in.nextDouble();
		
		System.out.println("Enter the Smaller time window:");
    	s_time = in.nextDouble();

		 at = hhmm_a/100 * 60 *60;
  		 at = at + hhmm_a%100 * 60;
    	 atf = at + ssss_a;


         ot = hhmm_o/100 * 60 * 60;
	     ot = ot + hhmm_o%100 *60;
	     otf = ot + ssss_o;

	     it = hhmm_i/100 * 60 *60;
         it = hhmm_i%100 * 60;
  		 itf = it + ssss_i;


	     secf = atf - otf;
	     System.out.println("The Travel time is :" + secf);
		 cd = 2 * secf;

		 diffs = itf - otf;
		 if(diffs > 0){
		 	cd = cd - diffs;
		 }
	/*	 else{
		 	cd = cd + diffs;
		 }*/
		 System.out.println("Number of units :" + cd * 20);

		 return cd;
	}	  


	public int calcSize(double bt, double st,double coda_time){
		int i=0,number_windows=0;
		double up_lim=0;
		//calculate the upper time for the small time window for the first one
		double small_up_lim = coda_time + st;
		//calculate the upper time limit for the bigger time window 
		double big_up_lim = coda_time + bt;
		//loop till all the possible smaller time windows are exhausted
		while(small_up_lim <= big_up_lim){
			number_windows++;
			small_up_lim = small_up_lim + st/2;
		}
		//return the number of windows posssible in the give bigger time window
		return number_windows;

	}

	public void rmsCalc(List<Integer> num, int size){
		int i,partition,start=0;
	    int sq_sum=0;
	    int flag = 0;
	    double rt_input;
	    stop = 0;
	    rms = new double[size];

	    for(partition=0;partition<size;partition++){
	        sq_sum = 0;
	        start =  partition*(int)((s_time*20)/2);	            
		        for(i= start;i<start+(s_time*20) && i < arr_size;i++){
		        		sq_sum = sq_sum + (num.get(i)*num.get(i));
		            	
		        }
		        if(i == start + (s_time*20)){
		        	flag = 1;
		        	stop++;
		        }
		        else{
		        	break;
		        }

   	
	        System.out.println(sq_sum);
	        rt_input = sq_sum/2;
	        System.out.println(rt_input);
	        rms[partition] = Math.sqrt(rt_input);
	        System.out.println(rms[partition]);

	    }
	    if(stop < size){
	    	System.out.println("WARNING : The Time value entered exceeds the data points available");
	    }
	}

	public void timeCalc(double coda_time){
		int i;
		time = new double[stop];
		try
            {
		  File fac = new File("time.txt");
                if (!fac.exists())
                {
                    fac.createNewFile();
                }
                System.out.println("\n----------------------------------");
                System.out.println("The TIME file has been created.");
                System.out.println("------------------------------------");
                
                FileWriter wr = new FileWriter(fac);
	        time[0] = coda_time + (s_time/2);
	        wr.write(time[0]+System.getProperty( "line.separator" ));
	        System.out.println("Time :" + 0 + " : " + time[0]);
	    for(i=1;i<stop;i++){
	        time[i] = time[i-1] + (s_time/2);
	        wr.write(time[i]+System.getProperty( "line.separator" ));
	        System.out.println("Time :" + i + " : " + time[i]);
	    }
	     wr.close();
	      }
            catch (IOException e)
            {
                e.printStackTrace();
            }
	}
	
	public void kCalc(double coda_time){
		int i;
		k_values = new double[stop];
		double[] x = new double[stop];
	    for(i=0;i<stop;i++){
	        x[i] = time[i]/(coda_time/2);
	        System.out.println("x :" + i + " : " + x[i]);
	        k_values[i] =  (1/x[i]) * (Math.log((x[i] + 1) / (x[i] - 1))); 
	        System.out.println("k(x) :" + i + " : " + k_values[i]);
	    }
	}

	
	public void lhsCalc(){
		int i;
		lhs = new double[stop];
		try
            {
	    File fac = new File("lhs.txt");
                if (!fac.exists())
                {
                    fac.createNewFile();
                }
                System.out.println("\n----------------------------------");
                System.out.println("The LHS file has been created.");
                System.out.println("------------------------------------");
                
                FileWriter wr = new FileWriter(fac);

	    for(i=0;i<stop;i++){
	        lhs[i] = Math.log(rms[i]/k_values[i]);
	        wr.write(lhs[i]+System.getProperty( "line.separator" ));                 
	        System.out.println("lhs :" + i + " : " + lhs[i]);
	    }
        wr.close();
         }
            catch (IOException e)
            {
                e.printStackTrace();
            }
	}

	public void QCalc(double m,double freq){
		//Calculate the Qc 
		double qc = -(freq / (2 * m));
		System.out.println("Qc is : " + qc);
	}

}
