	  //*****************************
	  // Scanpath comparison programs
	  //*****************************
	  //coded by Tom Foulsham (lpxtf@psychology.nottingham.ac.uk)

// SCANPATH AREA CLASS
//a class for building GUIs which displays a region and two scanpaths overlaid onto it

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import javax.swing.event.MouseInputListener;

public class scanpathArea extends JLabel
                                       implements MouseInputListener {
	    //this is an abstract class which shows an area with coordinates
        fixationSequence[] scanpaths = new fixationSequence[2];	//2 fixation sequences which can be displayed
        comparisonGUI selector;	//the host component
        int scanpathNumber;
        int preferredX, preferredY;
        boolean gridOn; //a flag for whether to show the grid or not
        int gridx=5,gridy=5;	//allows the size of the grid to be varied
    
        public scanpathArea(int preferredX,int preferredY,comparisonGUI selector,ImageIcon i) {
            this.selector = selector;
            Dimension preferredSize = new Dimension(preferredX,preferredY);	//initialises a variable to give the preferred size of the area
            this.preferredX=preferredX;
            this.preferredY=preferredY;
        	scanpaths[0]= new fixationSequence(selector.FIXES_PER_STRING);
        	scanpaths[1]= new fixationSequence(selector.FIXES_PER_STRING);            
            scanpathNumber=0;
            addMouseListener(this);
            setIcon(i);
        }
    
        public Dimension getPreferredSize() {
	        //a convenience method to return the size
	        Dimension preferredSize = new Dimension(preferredX,preferredY);
            return preferredSize;
        }
    
        protected void paintComponent(Graphics g) {
	        //First paint the component, including the background picture if its there
	        super.paintComponent(g);

            //Paint grid.
            if(gridOn)
            {
            g.setColor(Color.BLACK);
            int xdim=preferredX/gridx;
            int ydim=preferredY/gridy;
            drawGrid(g, xdim,ydim);
        	}
            
            //If user has chosen a point, paint a small dot on top.
            for (int s=0; s<2; s++)
            {
	            for (int i=0; i<scanpaths[0].maxFixNum; i++)
	            {    
	            if (scanpaths[s].fixations[i] != null) 
	            {
		            
		            if (s==0){g.setColor(Color.RED);}    //NB colour of scanpaths          
	                else{g.setColor(Color.GREEN);}
	                g.fillRect(scanpaths[s].fixations[i].x - 4, scanpaths[s].fixations[i].y - 4, 9, 9);
	                
	                		if(i>0)
	                		{
		                	//draw a line indicating a saccade
		                	g.drawLine(scanpaths[s].fixations[i].x,scanpaths[s].fixations[i].y,scanpaths[s].fixations[i-1].x,scanpaths[s].fixations[i-1].y);	
	                		}
	                if((i+1)<10)
	                {
		              //calls a method which draws a number next to the scanpath
	                drawNumber(g,i+1,scanpaths[s].fixations[i].x,scanpaths[s].fixations[i].y-15);
                	}
                	else
                	{
	                int tens=(int)(i+1)/10;
	                drawNumber(g,tens,scanpaths[s].fixations[i].x,scanpaths[s].fixations[i].y-15);
	                int units=(i+1)%10;
	                drawNumber(g,units,scanpaths[s].fixations[i].x+10,scanpaths[s].fixations[i].y-15);	
                	}
	            }
            	}
            }
        }
        
        //Draws a grid using the current color.
        private void drawGrid(Graphics g, int gridSpaceX, int gridSpaceY) {
	        //arguments give graphics context and number of divisions to make
            Insets insets = getInsets();
            int firstX = insets.left;
            int firstY = insets.top;
            int lastX = getWidth() - insets.right;
            int lastY = getHeight() - insets.bottom;
            
            //Draw vertical lines.
            int x = firstX;
            while (x < lastX) {
                g.drawLine(x, firstY, x, lastY);
                x += gridSpaceX;
            }
            
            //Draw horizontal lines.
            int y = firstY;
            while (y < lastY) {
                g.drawLine(firstX, y, lastX, y);
                y += gridSpaceY;
            }
        }
        
        //a method to draw numbers next to each fixation, x and y are the top right corner
        public void drawNumber(Graphics g, int num, int x, int y)
        {
	        g.setColor(Color.BLACK);
	        boolean[]posits =new boolean[7];
	        if(num<10)
	        {
		    System.arraycopy(getPosits(num), 0, posits, 0, 7);

		        for(int posit=0;posit<7;posit++)
		        {
			        if(posits[posit])
			        {
				     	switch (posit)
				     	{
					     	case 0:	g.drawLine(x,y,x+5,y);break;
					     	case 1:	g.drawLine(x+5,y,x+5,y+5);break;
					     	case 2:	g.drawLine(x+5,y+5,x+5,y+10);break;
					     	case 3:	g.drawLine(x+5,y+10,x,y+10);break;
					     	case 4:	g.drawLine(x,y+10,x,y+5);break;
					     	case 5:	g.drawLine(x,y+5,x,y);break;
					     	case 6:	g.drawLine(x,y+5,x+5,y+5);break;
				     	}   
			        }
		        }
        	}
        }
        
        //This method returns an array of booleans indicating which of the 7 digital positions are filled by the number argument
        public boolean[] getPosits(int num)
        { 
	    boolean[][] posits={{true,true,true,true,true,true,false},{false,true,true,false,false,false,false},{true,true,false,true,true,false,true},
	    	{true,true,true,true,false,false,true},{false,true,true,false,false,true,true},{true,false,true,true,false,true,true},
	    	{true,false,true,true,true,true,true},{true,true,true,false,false,false,false},{true,true,true,true,true,true,true},
	    	{true,true,true,false,false,true,true}};      
	    return posits[num];
        }

        public void clearArea()
        {
	            scanpaths[0].resetSequence();
	            scanpaths[1].resetSequence();
	            scanpathNumber=0;  
            repaint();
     	}
   
        public void setBackground(ImageIcon i)
        {
	        setIcon(i);
        }
            
        //Methods required by the MouseInputListener interface.
        public void mouseClicked(MouseEvent e) 
        {
	        //when clicked, store the point, print to console and add to current scanpath
	        Point temp;
	        if(scanpathNumber<2)
	        {
            int x = e.getX();
            int y = e.getY();

	            System.out.print(scanpaths[scanpathNumber].specNum+"x:"+x+"y:"+y+"\t");
	            temp=new Point(x,y);
	            scanpaths[scanpathNumber].addFix(temp);
                selector.updateClickPoint(scanpaths[scanpathNumber].fixations[1]); //feed back to host component
            repaint();
        	}
        }

        public void mouseMoved(MouseEvent e) {}
        public void mouseExited(MouseEvent e) {}
        public void mouseReleased(MouseEvent e) { }
        public void mouseEntered(MouseEvent e) { }
        public void mousePressed(MouseEvent e) { }
        public void mouseDragged(MouseEvent e) { }
    }
