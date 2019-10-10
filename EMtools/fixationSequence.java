	  //*****************************
	  // Scanpath comparison programs
	  //*****************************
	  //coded by Tom Foulsham (lpxtf@psychology.nottingham.ac.uk)

// FIXATION SEQUENCE CLASS
// A general class for a sequence of fixations
//		NB. Unique assignment method requires PermutationGenerator.java class

import java.awt.*;
import java.lang.Math;

public class fixationSequence 
{
	int maxFixNum,specNum=0,startFixNum=0;	//the maximum number of fixations in the sequence, and the number specified
	String scanpathString;	//a string of characters representing the sequence
	Point [] fixations;	//the coordinates in pixels for a series of fixations
	Double Drand,D,Is; //random and average distances for the comparison methods
	
	public fixationSequence(int n)
	//constructor signifying the maximum number of fixations
	{
		this.maxFixNum=n;//restricts memory allocation
		this.fixations=new Point[maxFixNum];
		System.out.println("Making fixations array of " +fixations.length);
	}
	
	public boolean addFix(Point p)
	//a method to be called every time a point is added
	{
		if(specNum<maxFixNum)
		{
				fixations[specNum]=p;
				specNum++;
				return true;			
		}
		else
		{
				System.out.println("The sequence is full!");
				return false;	
		}
		
	}
	
	public String printSp()
	//a method to display the scanpath coordinates to the console
	{
		String temp="";
		for(int i=startFixNum;i<specNum;i++)
		{
		temp=(temp + i + ": " + fixations[i].x + " , " + fixations[i].y + " ");	
		}
		return temp;
	}
	
	public String pointsToString(int dimx,int dimy, int gridx, int gridy)
	//a method to convert the stored fixations into a character string to enable Levenshtein distance calculations
		//Args give size of display and size of grid
		//NB rounding may give slightly different results!
	{
		char[][] regionLabels= new char[gridx][gridy];//to store region scheme
		int tempcode=65;
	    for (int y=0; y<gridy; y++)
	    {
		 	for (int x=0; x<gridx; x++)
		 	{
			 	regionLabels[x][y]=((char)tempcode);
			 	tempcode++;	
		 	}   
	    }
	    scanpathString="";
			    
	    for (int p=startFixNum; p<specNum; p++)
	    {
		   	int regionCodeX=fixations[p].x/(dimx/gridx);
			int regionCodeY=fixations[p].y/(dimy/gridy);
		    scanpathString=scanpathString+regionLabels[regionCodeX][regionCodeY];
	    }	    				
	    return scanpathString;				
	}
	
	public Point[] stringToPoints(int dimx,int dimy, int gridx, int gridy)
	//further convenience method for converting from a string to points
		//ASSUMES MID POINT WITHIN CHARACTER REGION
	{
		int tempcode=0,x,y;
		System.out.println("got here with "+scanpathString);
		Point p;
		for (int a=0; a<scanpathString.length(); a++)
		{
			tempcode=((int)scanpathString.charAt(a))-65;
			y=tempcode/gridx;
			x=tempcode%gridx;
			p=new Point((x*(dimx/gridx))+((dimx/gridx)/2),y*(dimy/gridy)+((dimx/gridx)/2));
			System.out.println("Added fixation" + a + p.toString());
			addFix(p);
		}
		
		return fixations;
	}
	
	public void resetSequence()
	{
		//removes all data from this instance		
		scanpathString="";
		for (int p=0; p<specNum; p++)
		{
			fixations[p]=null;
		}
		specNum=0;
	}
	
	public void excludeFirstFixation()
	{
		//start all further methods from 1, thereby excluding the very first fixation
		startFixNum=1;	
	}
	
	//SEQUENCE COMPARISON METHODS
	
	public double mannanDistance(fixationSequence scanpathB)
	//returns the mean distance between fixations and their nearest neighbours in another set
	{
		double d=0.0;
		for(int Afixes=startFixNum;Afixes<specNum;Afixes++)
		{
		double temp=10000.0;
			for (int Bfixes=scanpathB.startFixNum;Bfixes<scanpathB.specNum;Bfixes++)
			{
				if(fixations[Afixes].distance(scanpathB.fixations[Bfixes])<temp)
				{
				temp=fixations[Afixes].distance(scanpathB.fixations[Bfixes]);
				System.out.println("distance A"+Afixes+" to B"+Bfixes+" ="+temp);
				}
			}
		d=d+temp;
		}
		d=d/specNum;
		return d;
	}
	
	public double fullMannanFormula(fixationSequence scanpathB,int xdim, int ydim)
	//the complete formula for working out the standardised Is parameter
	{
	//make a matrix of every distance
	int n1=specNum,n2=scanpathB.specNum;
	double[][] distanceMatrix=new double [n1][n2];
		for(int i=startFixNum;i<n1;i++)
		{
			for(int j=scanpathB.startFixNum;j<n2;j++)
			{
				distanceMatrix[i][j]=fixations[i].distance(scanpathB.fixations[j]);
				System.out.print(distanceMatrix[i][j]+"\t");
			}
		System.out.println();
		}
	//find the lowest in every column.....
	double lowest,d2j=0,d1i=0;
		for(int i=startFixNum;i<n1;i++)
		{
		lowest=100000;
			for(int j=scanpathB.startFixNum;j<n2;j++)
			{
				if (distanceMatrix[i][j]<lowest){lowest=distanceMatrix[i][j];}
			}
		d1i=d1i+(lowest*lowest);
		System.out.println(d1i);
		}	
		
	//and in every row....
		for(int j=scanpathB.startFixNum;j<n2;j++)
		{
		lowest=100000;
			for(int i=startFixNum;i<n1;i++)
			{
				if (distanceMatrix[i][j]<lowest){lowest=distanceMatrix[i][j];}
			}
		d2j=d2j+(lowest*lowest);
		System.out.println(d2j);
		}
	n1=specNum-startFixNum;
	n2=scanpathB.specNum-scanpathB.startFixNum;
	double dsquared=(n1*d2j)+(n2*d1i);
	dsquared=dsquared/((2*n1*n2)*((xdim*xdim)+(ydim*ydim)));
	double d=Math.sqrt(dsquared);		
	return d;
	}
	
	public double UADistance(fixationSequence scanpathB)
	//returns the mean unique assignment distance between fixations and their nearest neighbours in another set
	{
		if(scanpathB.specNum==specNum)
		{
		double lowestd=100000.0,d=0;
		PermutationGenerator pairings = new PermutationGenerator (specNum-startFixNum);
			//an instance of the permutations class which will allow all possible combinations to be compared

		//work through possible pairings...
		int[] pairingArray;
		
		//System.out.print("Total number of permutations: "+pairings.getTotal()+"\n");
		while (pairings.hasMore ()) 
		{
		//for each pairing permutation
		//
		d=0;
		pairingArray = pairings.getNext ();	//gets the permutations
			for(int x=startFixNum;x<specNum;x++)	
			//for each fixation, pair x with the permutation in the other scanpath
			{
				//System.out.print("A:"+x+" B:" +pairingArray[x]+"\n");
				d=d+(fixations[x].distance(scanpathB.fixations[pairingArray[x]+startFixNum]));				
			}
		
		d=d/(specNum-startFixNum);	//The average distance using these pairings
			if(d<lowestd)
			{
			lowestd=d;	
			}
		
		}
		
		return d;
		}
		else
		{
		return -1;	
		}

	}
	
	public double fullUAFormula(fixationSequence scanpathB,int xdim, int ydim)
	//the complete formula for working out the standardised Is parameter
	{
		if(scanpathB.specNum==specNum)
		{		
		//work through possible pairings...
		double lowestd=10000000.0,d=0;
		int permCount=1;
		PermutationGenerator pairings = new PermutationGenerator (specNum-startFixNum);
		int[] pairingArray;
		int[] lowestPairingArray = new int[specNum-startFixNum];
		double d2j,d1i;
		System.out.print("Total number of permutations: "+pairings.getTotal()+"\n");
			while (pairings.hasMore ()) 
			{
			System.out.println("Permutation number:"+permCount);
			d=0;
			d2j=0;
			d1i=0;
			pairingArray = pairings.getNext ();	//gets the permutations
			System.out.println(pairingArray.length);
			System.out.println(scanpathB.specNum);
			System.out.println(specNum);
				for(int x=startFixNum;x<specNum;x++)	//for each fixation, pair x with the permutation in the other scanpath
				{
					//System.out.println(pairingArray[x]);
					d=fixations[x].distance(scanpathB.fixations[pairingArray[x-startFixNum]]);
					System.out.println("Comparing A"+x+" and B"+(pairingArray[x-startFixNum])+" distance is "+d);
					d1i=d1i+(d*d);
					d2j=d2j+(d*d);										
				}
				System.out.println("Sum of d1i/d2j squared for this pairing: "+d1i);
				if(d1i<lowestd)	//stores the lowest total distance amongst all pairings
				{
				lowestd=d1i;	
				lowestPairingArray=pairingArray;
				}
			permCount++;
			}			
		System.out.println("Sum of d1i/d2j squared for the lowest pairing: "+lowestd);
		d1i=lowestd;
		d2j=lowestd;	
		int n1=specNum-startFixNum;
		int n2=scanpathB.specNum-scanpathB.startFixNum;		
		double dsquared=(n1*d2j)+(n2*d1i);
		dsquared=dsquared/((2*n1*n2)*((xdim*xdim)+(ydim*ydim)));
		d=Math.sqrt(dsquared);		
		return d;
		}
		else
		{return -1;}
	
	}
	
	public double randomScanpaths(int simulations, int metricNum, int fixnumA,int fixnumB,int x, int y)
	//generates random scanpath pairs and returns the requested metric
	//arguments give the number of simulations, a code for the metric, the number of fixations in each scanpath and the dimensions
	{
		fixationSequence [] randSet1=new fixationSequence[simulations];
		fixationSequence [] randSet2=new fixationSequence[simulations];
		Point point,point2;
			for(int n=0;n<simulations;n++)
			{
				randSet1[n]=new fixationSequence(fixnumA);
				randSet2[n]=new fixationSequence(fixnumB);
				for(int f=0;f<fixnumA;f++)
				{
					point=new Point((int)(x*java.lang.Math.random()),(int) (y*java.lang.Math.random()));
					randSet1[n].addFix(point);
				}
				for(int f=0;f<fixnumB;f++)
				{
					point2=new Point((int)(x*java.lang.Math.random()),(int) (y*java.lang.Math.random()));
					randSet2[n].addFix(point2);
				}				
			}
			
		double avMetric=0;	
			for(int n=0;n<simulations;n++)
			{	switch (metricNum)
				{
				case 1:avMetric=avMetric+randSet1[n].fullMannanFormula(randSet2[n],x,y);break;
				case 2:avMetric=avMetric+randSet1[n].fullUAFormula(randSet2[n],x,y);break;	
				}
				
			}		
		avMetric=avMetric/simulations;
		return avMetric;
	}
	//NB 100 SIMULATIONS USED MAY BE TOO FEW AND NOISY.  
	//WARNING! FULL CALCULATIONS MAY TAKE A LONG TIME!
	
	public double fullIs(fixationSequence scanpathB,int xdim, int ydim)
	//puts the mannan formula together with the random generator to get the "Is" value
	{
	D=fullMannanFormula(scanpathB, xdim, ydim);
	Drand=randomScanpaths(100,1,(specNum-startFixNum),(scanpathB.specNum-scanpathB.startFixNum),xdim,ydim);
	Is=100*(1-(D/Drand));
	return Is;
	}
	
	public double fullUA(fixationSequence scanpathB,int xdim, int ydim)
	//puts the UA formula together with the random generator
	{
	D=fullUAFormula(scanpathB, xdim, ydim);
		System.out.println("Scanpath UAD:"+D);
	Drand=randomScanpaths(100,2,(specNum-startFixNum),(scanpathB.specNum-scanpathB.startFixNum),xdim,ydim);
		System.out.println("Random UAD:"+Drand);
	Is=100*(1-(D/Drand));
	return Is;
	}	
	
}
