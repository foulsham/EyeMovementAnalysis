	  //*****************************
	  // Scanpath comparison programs
	  //*****************************
	  //coded by Tom Foulsham (lpxtf@psychology.nottingham.ac.uk)

// IMPROVED STRING EDIT EXTRA CLASS
//computes a WEIGHTED version of the Levenshtein distance between two (scanpath) strings
//each operation is weighted by the associated linear distance between two fixations

import java.awt.*;
import java.lang.Math;


class stringEditExtra
{
String first,second;//the strings
fixationSequence fixseq1,fixseq2;//the corresponding fixation sequences
int dimx,dimy;	//dimensions of the visual area
double dist,normdist,similarity;	//similarity stats

	public stringEditExtra(fixationSequence fixseq1, fixationSequence fixseq2,int dimx,int dimy, int gridx, int gridy)
	//CONSTRUCTOR..... takes fixation sequences which hold all point and string information.  Also needs to know dimensions
	{
		this.fixseq1=fixseq1;
		this.fixseq2=fixseq2;
		this.dimx=dimx;
		this.dimy=dimy;
		this.first=fixseq1.pointsToString(dimx,dimy,gridx,gridy);	//uses grid and display dimensions to generate strings
		this.second=fixseq2.pointsToString(dimx,dimy,gridx,gridy);
	}
	
	public stringEditExtra(String stringA,String stringB, fixationSequence fixseq1, fixationSequence fixseq2,int dimx,int dimy)
	//ALTERNATIVE CONSTRUCTOR.....takes user defined strings (necessary when using ROIs not defined by a grid) 
	{
		this.fixseq1=fixseq1;
		this.fixseq2=fixseq2;
		this.dimx=dimx;
		this.dimy=dimy;
		this.first=stringA;
		this.second=stringB;
	}	
	
	public int checkcentre(String currentstring)	//this method returns 1 or 0 for a central (M) fix
	{
		if(currentstring.charAt(0)=='M'){return 1;}
		else{return 0;}
	}
        
        public String removefirstfix(String currentstring)	//this method will remove first fix (ie. delete char0)
        {							//"currentstring" is an argument rep'ing the string to modify
        return currentstring.substring(1,currentstring.length());
        }
        
        public String condenser(String currentstring)	//this method will condense consecutive letters into one       											
        {
	      String condensedstring;	//this to hold the modified string
	      if(currentstring!="")
	      {
            condensedstring= ""+currentstring.charAt(0);	//has to start with first letter  
            for(int charcounter=1;charcounter < currentstring.length();charcounter++)
            {
                if(currentstring.charAt(charcounter)!=currentstring.charAt(charcounter-1))	//tests for consecutive letters which are NOT the same
                {
                condensedstring=condensedstring+currentstring.charAt(charcounter);	//adds to modified variable
                }	//if equal, won't add!
            }
      	  }
          else
          {
          condensedstring= ""; //this for any rare cases where string is empty
       	  } 
            return condensedstring;	//returns the modified version
        }
        
        public String shortener(String currentstring,int desiredlength)	
        //this additional method will shorten strings. int argument gives the length.
        {
            String shortenedstring;
            shortenedstring= ""+currentstring.charAt(0);
            for(int charcounter=1;charcounter<desiredlength;charcounter++)
            {
            shortenedstring=shortenedstring+currentstring.charAt(charcounter);
            }
            return shortenedstring;
        }
        
        public String removeinvalidchars(String currentstring)
        //additional method to remove invalid chars which arise from fixations outside screen
        {
            String modifiedstring;
            modifiedstring= "";	//initialises to empty 
            for(int charcounter=0;charcounter<currentstring.length();charcounter++)
            {
                Character currentchar=new Character(currentstring.charAt(charcounter));	//wraps the character in order to use the letter test method
                if (currentchar.isLetter(currentstring.charAt(charcounter)) && (currentstring.charAt(charcounter)!='Z'))	//then it is valid so keep it
                {
                modifiedstring=modifiedstring+currentchar;
                }
            } 
            return modifiedstring;
        } 
                    
        public double distanceCost(int i,int j)	
        //THIS METHOD WILL RETURN A VALUE BETWEEN 0 AND 1 INDICATING THE NORMALISED DISTANCE BETWEEN TWO POINTS
        {
			double c=fixseq1.fixations[i+fixseq1.startFixNum].distance(fixseq2.fixations[j+fixseq2.startFixNum])/(Math.sqrt((dimx*dimx)+(dimy*dimy)));
			return c;
        }
        
        public double findlowest(double int1,double int2,double int3)	//this method finds the lowest of three integers
		{
            if (int2<int1)
            {
            int1=int2;
            }
            if (int3<int1)
            {
            int1=int3;
            }
            return int1;
        }
        
	public double comparison()
	//PUT ALL THE WORK IN ONE METHOD OF THE SAME CLASS...
	{
	
	  //*****************************
	  // Compute Levenshtein distance
	  //*****************************
	  //modified from code at http://www.merriampark.com/ld.htm
	
	  double d[][]; // matrix is now of doubles, allowing variable costs
	  int n; // length of first
	  int m; // length of second
	  int i; // iterates through first
	  int j; // iterates through second
	  char s_i; // ith character of first
	  char t_j; // jth character of second
	  double cost; // cost
	
	    // Step 1
	
	    n = first.length ();
	    m = second.length ();
	    if (n == 0) {
	      return m;	//if either string is empty, return length of the other
	    }
	    if (m == 0) {
	      return n;
	    }
	    d = new double[n+1][m+1];
	
	    // Step 2
	
	    for (i = 0; i <= n; i++) {
	      d[i][0] = i;
	    }
	
	    for (j = 0; j <= m; j++) {
	      d[0][j] = j;
	    }
	
	    // Step 3
	
	    for (i = 1; i <= n; i++) {
	
	      s_i = first.charAt (i - 1);
	
	      // Step 4
	
	      for (j = 1; j <= m; j++) {
	
	        t_j = second.charAt (j - 1);
	
	        // Step 5
	
	        if (s_i == t_j) {
		        		//Cost is now variable, depending on the proximity of the current fixations
		        		//If in same region, normalised distance will be close to 0
	          cost = distanceCost(i-1,j-1);
	        }
	        else {
	          cost = distanceCost(i-1,j-1); //If in different region, normalised distance will be closer to 1
	        }
	
	        // Step 6
	
	        d[i][j] = findlowest (d[i-1][j]+1, d[i][j-1]+1, d[i-1][j-1] + cost);
			System.out.print(d[i][j]+"\t");
	      }
		System.out.println();
	    }
	
	    // Step 7
	
	    dist= d[n][m];
	       	if (n>=m){normdist=dist/n;}
	    	else {normdist=dist/m;}
	    similarity=1-normdist;
	    return similarity;
	
	  }

}