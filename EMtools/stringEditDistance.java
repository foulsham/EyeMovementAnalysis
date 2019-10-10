	  //*****************************
	  // Scanpath comparison programs
	  //*****************************
	  //coded by Tom Foulsham (lpxtf@psychology.nottingham.ac.uk)

// STRING EDIT DISTANCE CLASS
//computes the Levenshtein distance between two (scanpath) strings

class stringEditDistance
{
String first,second;	//the two strings
double dist,normdist,similarity;	//the distance, normalised distance and similarity computed by the algorithm

	public stringEditDistance(String first, String second)
	//constructor.....
	{
		this.first=first;
		this.second=second;
	}
	
	public int checkcentre(String currentstring)	
	//this method returns 1 or 0 for an M fix at the start
	{
		if(currentstring.charAt(0)=='M'){
			return 1;	
		}
		else{
			return 0;
		}
	}
        
        public String removefirstfix(String currentstring)	
        //this method will remove first fix (ie. delete char0)
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
        //this method will shorten strings. int argument gives the length.
        {
            String shortenedstring;
            shortenedstring= ""+currentstring.charAt(0);           
            for(int charcounter=1;charcounter<desiredlength;charcounter++)
            {
            shortenedstring=shortenedstring+currentstring.charAt(charcounter);
            }
            System.out.println(shortenedstring);
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
        
        
        public int charcomparison(char char1, char char2)	//this method will return 1 or 0 depending upon the equality of two characters
        {
                if (char1==char2)	//this bit assesses char similarity 
                {
                        return 0;	
                }
                else
                {
                        return 1;	
                }
        }
        
        public int findlowest(int int1,int int2,int int3)	//this method finds the lowest of three integers
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
	//PUT ALL THE COMPARISON WORK IN ONE METHOD
	{
	
	  //*****************************
	  // Compute Levenshtein distance
	  //*****************************
	  //modified from code at http://www.merriampark.com/ld.htm
	
	  int d[][]; // matrix
	  int n; // length of first
	  int m; // length of second
	  int i; // iterates through first
	  int j; // iterates through second
	  char s_i; // ith character of first
	  char t_j; // jth character of second
	  int cost; // cost
	
	    // Step 1
	
	    n = first.length ();
	    m = second.length ();
	    if (n == 0) {
	      return m;	//if either string is empty, return length of the other
	    }
	    if (m == 0) {
	      return n;
	    }
	    d = new int[n+1][m+1];
	
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
	          cost = 0;
	        }
	        else {
	          cost = 1;
	        }
	
	        // Step 6
	
	        d[i][j] = findlowest (d[i-1][j]+1, d[i][j-1]+1, d[i-1][j-1] + cost);
	
	      }
	
	    }
	
	    // Step 7
	
	    dist= d[n][m];
	       	if (n>=m){normdist=dist/n;}
	    	else {normdist=dist/m;}
	    similarity=1-normdist;
	    return similarity;
	
	  }

}