	  //*****************************
	  // Scanpath comparison programs
	  //*****************************
	  //coded by Tom Foulsham (lpxtf@psychology.nottingham.ac.uk)

/*	STRING COMPARISON INTERFACE
*
*	GUI which does the following:
*		-asks for two strings or scanpaths
*		-displays the scanpath they represent
*		-compares them using the string editing algorithm
*		-gives D statistics
*					
*	Requires:
*		-"stringEditDistance.class" which includes the general string editing prog
*		-"scanpathArea.class" which is a class for displaying scanpaths
*
*	Added in version 2:
*		-now gives a choice between similarity metrics
*		-so requires "fixationSequence.class" which is a class for representing sequence in terms of coordinates	
*/


import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.net.URL;
import java.awt.event.MouseEvent;
import javax.swing.event.*;
import java.io.*;
import java.applet.*;

public class comparisonGUI extends JApplet implements ActionListener, ItemListener,ChangeListener
{
	//PROGRAM CONSTANTS
	final static int AREA_SIZE_X=512;
	final static int AREA_SIZE_Y=384;
	static int NUM_OF_X_REGIONS = 5;	//ie the horizontal and vertical resolution for the grid, must give a product of less than 26
	static int NUM_OF_Y_REGIONS = 5;
	final static int FIXES_PER_STRING = 10;	//ie the length of each string
	
	//PROGRAM VARIABLES
	static int currentFix=0,scanpathNumber=0;	//counters to keep track of fixes
	static char[][] regionLabels= new char[NUM_OF_X_REGIONS][NUM_OF_Y_REGIONS];//to store region scheme
	
	//GUI COMPONENTS
	static JPanel mainPanel,controlPanel,imagePanel,comparePanel;	//creates panels
	static JLabel leftString, rightString, feedback;	//labels to show strings and comparison
    static JButton compareButton,resetButton,selectScanpath,selectString,doneButton;	//buttons
    static JRadioButton dButton, mButton,uaButton,dxButton;	//radio buttons for selecting different comparison modes
    static JTextField txtInput;	//a text box to input strings
    
    static scanpathArea bgImage=null;
    static JCheckBox showBg=new JCheckBox("Show background image");
    	//a check box which will control whether a background image is shown behind the scanpaths
    static JCheckBox showGrid=new JCheckBox("Show grid with selected dimensions");
    	//a check box which will control whether a grid is shown  	
    
    //a list of possible backgrounds
    static String[] bgs=new String[3];
    static JList bgList;
    static JComboBox bgCombo; 
    ImageIcon bground;
	
	//spin buttons to control the size of the grid
	static SpinnerModel modelX =new SpinnerNumberModel(5, 1, 20,1);
	static JSpinner gridXspin = new JSpinner(modelX);
	static SpinnerModel modelY =new SpinnerNumberModel(5, 1, 20,1);
	static JSpinner gridYspin = new JSpinner(modelY); 
	
    //CONSTRUCTOR WHICH BUILDS THE GUI
    public void initialiseGUI()
    {
	    //BUILD PANELS
	    mainPanel = new JPanel();
	    mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));
	    imagePanel = new JPanel(new GridBagLayout());	//holds image and scanpath
        controlPanel = new JPanel(new GridLayout(0,4,5,5));	//holds user controls       
        
		addWidgets();
            
        //Add the panels to the main panel.        
        mainPanel.add(controlPanel);        
        mainPanel.add(imagePanel);
        
        Container contentPane = getContentPane();
        contentPane.add(mainPanel);
    } 

    //method to add all the bits
    private void addWidgets()
    {
        
        //initialise and listen to the text fields
        txtInput=new JTextField();
        txtInput.setColumns(FIXES_PER_STRING);
        txtInput.setText("Type string here");
        txtInput.addActionListener(this);
        txtInput.setActionCommand("string change");
        txtInput.setEditable(false);
        
        showBg.addItemListener(this);
        showGrid.addItemListener(this);        
        
        //initialise the buttons    
        compareButton = new JButton("Compare scanpaths");
        resetButton = new JButton("Reset");
        selectScanpath = new JButton("Select scanpath");
        selectString = new JButton("Type string");
        doneButton = new JButton("Done");
        
        dButton=new JRadioButton("Use Levenshtein distance");
        mButton=new JRadioButton("Use Mannan distance");
        uaButton=new JRadioButton("Use U.A. distance");
        dxButton=new JRadioButton("Use string-editing extra");
        
		//Listen to events from these buttons.
        compareButton.addActionListener(this);
        compareButton.setActionCommand("compare");	//these are to distinguish between several events
        compareButton.setEnabled(false);
        
        resetButton.addActionListener(this);
        resetButton.setActionCommand("reset");	
        resetButton.setEnabled(true);
        
        selectScanpath.addActionListener(this);
        selectScanpath.setActionCommand("scanpath");	
        selectScanpath.setEnabled(false);
        
        selectString.addActionListener(this);
        selectString.setActionCommand("string");	
        selectString.setEnabled(true);
        
        doneButton.addActionListener(this);
        doneButton.setActionCommand("done");	
        doneButton.setEnabled(false);        

        dButton.setSelected(true); 
        
        //Group the radio buttons
        ButtonGroup modeSelectors = new ButtonGroup();
        modeSelectors.add(mButton);
        modeSelectors.add(dButton);	
        modeSelectors.add(uaButton);
        modeSelectors.add(dxButton);
        
        //Initialise the labels
        leftString=new JLabel ("?????");
        	leftString.setForeground(Color.RED);
        rightString=new JLabel ("?????");
        	rightString.setForeground(Color.GREEN);
        feedback=new JLabel ();
        	updateFeedback(0);
        	feedback.setHorizontalAlignment(SwingConstants.CENTER);
        
        	bgs[0]=new String("blank.jpg");
        	bgs[1]=new String("house.jpg");
        	bgs[2]=new String("trent.jpg");
        bgList=new JList(bgs);	
        bgCombo = new JComboBox(bgs);
        bgCombo.setSelectedIndex(0);
        bgCombo.addActionListener(this);
        bgCombo.setActionCommand("bgcombo");
                        	
        bground=new ImageIcon();
        bground=createImageIcon("/images/blank.jpg","blank");
        bgImage= new scanpathArea(AREA_SIZE_X,AREA_SIZE_Y,this,bground);
        //bgImage.setBackground(bground);
        bgImage.setBorder(BorderFactory.createLineBorder(Color.black));
        
        gridXspin.addChangeListener(this);
        gridYspin.addChangeListener(this);
        
        //Add stuff to panels.
        controlPanel.add(resetButton);
        controlPanel.add(selectScanpath);
        controlPanel.add(selectString);
        controlPanel.add(doneButton);
        controlPanel.add(dButton);
        controlPanel.add(mButton);
        controlPanel.add(uaButton);
        controlPanel.add(dxButton);
        
        //comparePanel.add(feedback);
        //comparePanel.add(compareButton);
        
        GridBagConstraints c = new GridBagConstraints();
        c.gridx=0;
        c.gridy=0;
        c.gridwidth=1;
        c.weightx=0.2;
        c.weighty=0.2;
        imagePanel.add(bgCombo,c);        
		c.gridx=1;
        c.gridwidth=1;        
		imagePanel.add(showBg,c);
				showBg.setSelected(false);
		c.gridx=2;
        c.gridwidth=2; 				
		imagePanel.add(showGrid,c);
				showGrid.setSelected(false);
		c.gridx=4;
        c.gridwidth=1; 				
		imagePanel.add(gridXspin,c);
		c.gridx=5;
        c.gridwidth=1; 				
		imagePanel.add(gridYspin,c);										
		c.gridx=0;
        c.gridy=1;
        c.gridwidth=6;
        c.weightx=1;
        c.weighty=1;
		imagePanel.add(bgImage,c);
		c.gridx=0;
        c.gridy=2;
        c.gridwidth=6;
        c.weightx=0.333;
        c.weighty=0.5;                 
        imagePanel.add(feedback,c); 		
		c.gridx=1;
        c.gridy=3;
        c.gridwidth=4;
        c.weightx=0.333;
        c.weighty=0.25;                 
        imagePanel.add(compareButton,c); 
        c.gridx=0;
        c.gridy=4;
        c.gridwidth=2;               
        imagePanel.add(leftString,c); 
        c.gridx=2;
        c.gridy=4;    
        c.gridwidth=2;   
        c.weightx=0.4;               
        imagePanel.add(txtInput,c);        
        c.gridx=4;
        c.gridy=4;   
        c.gridwidth=2;             
        imagePanel.add(rightString,c);           
    }
    
    
    //METHOD REQUIRED BY THE ACTION LISTENER TRIGGERED BY BUTTON PRESS
    public void actionPerformed(ActionEvent event) 
    {
	    if(event.getActionCommand().equals("compare")||event.getActionCommand().equals("string change"))
	    {	   
		    
		    if(event.getActionCommand().equals("string change"))
    		{
	    		gridXspin.setEnabled(false);
	    		gridYspin.setEnabled(false);
		    	System.out.println("typed "+txtInput.getText().toUpperCase()+" into scanpath " +scanpathNumber);
				bgImage.scanpaths[scanpathNumber].scanpathString=txtInput.getText().toUpperCase();
				bgImage.scanpaths[scanpathNumber].stringToPoints(AREA_SIZE_X,AREA_SIZE_Y,NUM_OF_X_REGIONS, NUM_OF_Y_REGIONS);
				bgImage.repaint();
				bgImage.scanpathNumber++;				
				leftString.setText(bgImage.scanpaths[0].scanpathString);
				rightString.setText(bgImage.scanpaths[1].scanpathString);								
				txtInput.setText("");
			    scanpathNumber++;
			    updateFeedback(scanpathNumber);
	    		doneButton.setEnabled(false);			    				
				compareButton.setEnabled(scanpathNumber==2);
			}
			else
			{
			mainPanel.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			feedback.setText("Comparing...");
			    if(dButton.isSelected())
			    {
				stringEditDistance mystrings=new stringEditDistance(leftString.getText(),rightString.getText());
				feedback.setText("Similarity score: "+mystrings.comparison());
				}
				
				if(dxButton.isSelected())
			    {
				stringEditExtra mystrings=new stringEditExtra(bgImage.scanpaths[0],bgImage.scanpaths[1],AREA_SIZE_X,AREA_SIZE_Y,NUM_OF_X_REGIONS, NUM_OF_Y_REGIONS);
				feedback.setText("Similarity score: "+mystrings.comparison());
				}
				
			    if(mButton.isSelected())
			    {
				//this bit for rounding
				java.text.DecimalFormat f = new java.text.DecimalFormat("###.####");			
				feedback.setText("Is score: "+f.format(bgImage.scanpaths[0].fullIs(bgImage.scanpaths[1],AREA_SIZE_X,AREA_SIZE_Y)));
				}		
				
				if(uaButton.isSelected())
			    {			
				    if(bgImage.scanpaths[0].specNum==bgImage.scanpaths[1].specNum)
				    {
					java.text.DecimalFormat f = new java.text.DecimalFormat("###.####");
					feedback.setText("Unique assignment similarity score: "+f.format(bgImage.scanpaths[0].fullUA(bgImage.scanpaths[1],AREA_SIZE_X,AREA_SIZE_Y)));
					}
					else{feedback.setText("Scanpaths are different lengths! Comparison aborted.");}
				}
			mainPanel.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));		
			}
	    }
	    

        
        if (event.getActionCommand().equals("reset"))
        {
		resetPanels();
        }
        
        if (event.getActionCommand().equals("scanpath"))
        {
	    updateFeedback(0);
        selectString.setEnabled(true);
        selectScanpath.setEnabled(false);
        txtInput.setEditable(false);
        }                
        
        if (event.getActionCommand().equals("string"))
        {
	    updateFeedback(3);
        selectScanpath.setEnabled(true);
        selectString.setEnabled(false);
        txtInput.setEditable(true);
        }   
        
        if (event.getActionCommand().equals("done"))
        {
	    scanpathNumber++;
	    updateFeedback(scanpathNumber);
	    doneButton.setEnabled(false);
	    bgImage.scanpathNumber++;
	   	System.out.println("Scanpath complete....sNum="+scanpathNumber+"  bgSNum="+bgImage.scanpathNumber);
	   	compareButton.setEnabled(scanpathNumber==2);
        } 
        
        if (event.getActionCommand().equals("bgcombo")&&(showBg.isSelected()==true))
        {        
            bground=createImageIcon("/images/"+bgCombo.getSelectedItem(),(String)bgCombo.getSelectedItem());  
            bgImage.setBackground(bground);             
        }
                    
    }
    
    public void itemStateChanged(ItemEvent e) 
    {
	    Object source = e.getItemSelectable();
	    
	    if ((source==showBg)&&(e.getStateChange() == ItemEvent.SELECTED))
	    {
		 bground=createImageIcon("/images/"+bgCombo.getSelectedItem(),(String)bgCombo.getSelectedItem());  
		 bgImage.setBackground(bground); 		    
	    }
	    
	    if ((source==showBg)&& (e.getStateChange() == ItemEvent.DESELECTED))
	    {
		 bground=createImageIcon("/images/blank.jpg","blank");  
		 bgImage.setBackground(bground);   
	    }
	    
	    if ((source==showGrid)&& (e.getStateChange() == ItemEvent.SELECTED))
	    {
		    bgImage.gridOn=true;
		    bgImage.repaint();
	    }	
	    
	    if ((source==showGrid)&& (e.getStateChange() == ItemEvent.DESELECTED))
	    {
		    //bgImage.setGrid(true);
		    bgImage.gridOn=false;
		    bgImage.repaint();
	    }
    }
    	
	
	public void stateChanged(ChangeEvent e) 
	{
		bgImage.gridx=((SpinnerNumberModel)modelX).getNumber().intValue();
		bgImage.gridy=((SpinnerNumberModel)modelY).getNumber().intValue();
		NUM_OF_X_REGIONS=bgImage.gridx;
		NUM_OF_Y_REGIONS=bgImage.gridy;
        if (bgImage.gridOn) 
        {
			bgImage.repaint();
        }       
    }
    
    protected static ImageIcon createImageIcon(String path, String describer) 
    {
        java.net.URL imageURL = comparisonGUI.class.getResource(path);

        if (imageURL == null) {
            bgImage.setText("Resource not found: "
                               + path);
            return null;
        } else {
	                // resize the image and then display it
	        ImageIcon newIcon=new ImageIcon(imageURL,describer);
        	Image temp;
        	temp=newIcon.getImage();
        	temp=temp.getScaledInstance(AREA_SIZE_X, AREA_SIZE_Y,Image.SCALE_DEFAULT);
        	newIcon.setImage(temp);
            return newIcon;
        }
    }    
    
    //METHOD TO BE CALLED BY THE MOUSE LISTENER CLASS
 	public static void updateClickPoint(Point p)
    {
	    updateFeedback(4); 
	    gridXspin.setEnabled(false);
	    gridYspin.setEnabled(false);
	    doneButton.setEnabled(scanpathNumber<2);
    	currentFix++;
    	bgImage.scanpaths[scanpathNumber].pointsToString(AREA_SIZE_X,AREA_SIZE_Y,NUM_OF_X_REGIONS, NUM_OF_Y_REGIONS);
		leftString.setText(bgImage.scanpaths[0].scanpathString);
		rightString.setText(bgImage.scanpaths[1].scanpathString);    	
    }
    
    //METHOD TO RESET LABELS ETC.
    public void resetPanels()
    {
	    gridXspin.setEnabled(true);
	    gridYspin.setEnabled(true);
        selectScanpath.setEnabled(false);
        selectString.setEnabled(true);
        compareButton.setEnabled(false);
        txtInput.setEditable(false);
        txtInput.setText("Type string here");
        leftString.setText("?????");
        rightString.setText ("?????");
        updateFeedback(0);  
        currentFix=0;
        scanpathNumber=0;  
        bgImage.clearArea();   	    
    }
    
    //METHOD TOD DISPLAY INSTRUCTIONS
    public static void updateFeedback(int code)
    {
	 switch (code)
	 {
		case 0: feedback.setText("Select a scanpath by clicking on the image or choose 'Type' to enter a string.");break;
		case 1: feedback.setText("Select the second scanpath.");break;
		case 2: feedback.setText("Both scanpaths have been added, choose a comparison metric and click 'Compare'.");break;
		case 3: feedback.setText("Type the string into the box and press enter when string is entered.");break;
		case 4: feedback.setText("Click 'Done' to finish adding fixations to this scanpath.");break;		
		case 5: feedback.setText("Try another comparison or click 'Reset' to begin again.");break;
		default: feedback.setText("An error has ocurred.");break;	 
	 }   
    }
   
    public void init() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                initialiseGUI();
            }
        });
    }
}



