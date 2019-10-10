
function f=codeVideoWithKeys(interestAreas,frameDelay)
%CODEVIDEOWITHKEYS.M
%     Load a video coding GUI.
%     
%     H=CODEVIDEOWITHKEYS() loads the gui with only one control, a "Missing" button.
%     Returns a handle to the figure
%     
%     H=CODEVIDEOWITHKEYS(interestAreas) loads the gui with a series of buttons.  
%     Single argument should be a cell array of area labels. Last area will
%     be labelled "Missing" automatically.  Maximum is 8 areas.
%
%     H=CODEVIDEOWITHKEYS(interestAreas,frameDelay) loads the GUI with a
%     custom frame delay giving the number of seconds between each frame
%     when playing.  Default is 0.5.
%
%     The code stores an array of ones and zeros which can then be stored
%     as a text file.

%GUIDE messed up my GUI, so here's one made programmatically!

%make the main figure and the axis which will display the video
f=figure('CloseRequestFcn',@closeMainFigure,...& define a custom close function
    'KeyPressFcn',@iaKeyPress,...& define a function to receive key presses
    'MenuBar','none','DockControls','off',...& customize the menus
    'Name','Video coding application','NumberTitle','off',...& titles
    'Position',[100,100,700,800]);

%add the axis which will show the current video frame
axes('Units','pixels','Box','on','Position',[30,250,640,480]);
axis off

%add the general controls which will feature in all GUIs
%any which can gain focus must also have the key press callback
uicontrol(gcf,'Style','slider','Position',[30,200,640,30]); % a slider to frame seek
uicontrol(gcf,'Style','pushbutton','String','Choose video file...',...
    'Position',[40,140,140,60],'FontSize',14,'Tag','ChooseVid',...
    'Callback',@chooseVidButton,'KeyPressFcn',@iaKeyPress); % buttons to choose input and output files
uicontrol(gcf,'Style','pushbutton','String','Choose output file...',...
    'Position',[40,80,140,60],'FontSize',14,'Tag','ChooseText',...
    'Callback',@chooseTextButton,'Enable','off','KeyPressFcn',@iaKeyPress);
uicontrol(gcf,'Style','text','String','',...% text labels to show the files we're using
    'Position',[200,120,120,80],'FontSize',14,...
    'BackgroundColor',[0.80,0.80,0.80],'Tag','vidLabel');
uicontrol(gcf,'Style','text','String','',...
    'Position',[200,20,120,80],'FontSize',14,...
    'BackgroundColor',[0.80,0.80,0.80],'Tag','textLabel');
bg= uibuttongroup(gcf,'Title','Point-of-Gaze',...% a button group to store the IA buttons
    'Units','pixels','Position',[350,20,330,180],'FontSize',14,'BackgroundColor',[0.80,0.80,0.80]...
    );%'SelectionChangeFcn',@iaButtonPress);
uicontrol(gcf,'Style','pushbutton','String','Play movie',...
    'Position',[40,10,140,60],'FontSize',14,'Tag','playControl',...
    'Callback',@controlMovie,'Enable','off','KeyPressFcn',@iaKeyPress); % button to control the movie

%now add the IA buttons according to the input arguments
if nargin>0
    interestAreas=[interestAreas,{'Missing'}];
else
    interestAreas={'Missing'};
end
%get the frame delay for the timer, if we gave a second argument
global customDelay;
if nargin==2
    customDelay=frameDelay;
else
    customDelay=0.5;
end

p=[10,100];%position the controls, starting at the top left

%for each area....
for ia=1:length(interestAreas)
    
    %add a button to the button group
    uicontrol(gcf,'Style','radiobutton','Parent',bg,...
        'String', ['(',int2str(ia),') ',interestAreas{ia}],...%add the right label
        'Position',[p(1),p(2),80,40],'Enable','off',...
        'Tag',int2str(ia),'KeyPressFcn',@iaKeyPress);%add a callback to handle a button press
    p(1)=p(1)+90;
    if p(1)>190
        p(1)=10;
        p(2)=p(2)-40;
    end
end

%-----------------
%when the user presses the choose video button....
function chooseVidButton(hObject, eventdata, handles)
% hObject    handle to vidChooseButton (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)

%call up an open dialog to select the video filename
[FileName,PathName,FilterIndex] = uigetfile('*.avi','Choose the video file');

global CURRENT_VIDEO;
global t;
global customDelay;

if FileName~=0
    %display the filename in the static text
    h=findobj(gcf,'Tag','vidLabel');
    set(h,'String',FileName);

    %load the avi file, and store it as a global variable
    %NB: MMREADER FUNCTION IS NEW TO MATLAB AND MAY HAVE BEEN SUPERCEDED IN
    %MOST RECENT VERSIONS
    CURRENT_VIDEO=mmreader([PathName,FileName]);
    totalFrames=get(CURRENT_VIDEO,'NumberOfFrames');

    %also set up the position vector, and store this in the figure's user
    %data
    h=findobj(gcf,'Tag','textLabel');
    set(h,'String','');
    bg=findobj(gcf,'Type','uipanel');
    h=findobj(gcf,'Parent',bg);
    gazeVector=zeros(totalFrames,length(h)+1);
    gazeVector(:,1)=1:totalFrames;
    set(gcf,'UserData',gazeVector);
    
    
    %enable buttons
    for i=1:length(h)
        set(h(i),'Enable','on');
    end
    
    %set up a timer
    delay=customDelay;
    t = timer('TimerFcn',@timercallback, 'Period', delay,...
        'ExecutionMode','fixedDelay','StartDelay',delay,...
        'TasksToExecute',totalFrames);

    %set up the slider
    h=findobj(gcf,'Style','slider');
    set(h,'Min',1,'Max',totalFrames,'Value',1,...
        'SliderStep',[1/totalFrames,(1/totalFrames)*20],...
        'Callback',@advanceSlider);
    h=findobj(gcf,'Tag','ChooseText');
    set(h,'Enable','on');
    h=findobj(gcf,'Tag','playControl');
    set(h,'Enable','on');    
        
    %display the first frame
    displayFrame(1);
end

%-----------------
%when user presses the choose output button....
function chooseTextButton(hObject, eventdata, handles)
%call up a save dialog box to save the gaze vector in a tab delimited file
[FileName,PathName,FilterIndex] = uiputfile('*.txt','Choose a place to save the point-of-gaze data....');

if FileName~=0
    thisFileName=[PathName,FileName];
   u=get(gcf,'UserData'); 
    
   %write the headers first, using fprint
   fid=fopen(thisFileName,'wt');
   fprintf(fid,'%s\t','FrameNumber');
   bg=findobj(gcf,'Type','uipanel');
   for i=1:(size(u,2)-1)
       h=findobj(gcf,'Parent',bg,'Tag',int2str(i));
       s=get(h,'String');
       fprintf(fid,'%s\t',s);
   end
   fprintf(fid,'\n');
   fclose(fid);
   
   %write the data using dlmwrite
   dlmwrite(thisFileName,u,'-append','delimiter','\t');
   
   %show a message confirming the write
   totalMissing=sum(u(:,size(u,2)));
   totalAnnotated=sum(sum(u(:,2:size(u,2)),2));
   h=findobj(gcf,'Tag','textLabel');
   set(h,'String',['Data from ',int2str(totalAnnotated),' frames written to ',thisFileName]);
   disp(['Writing to ',thisFileName]);
   disp([int2str(totalAnnotated),' annotated frames...']);
   disp([int2str(totalMissing),' confirmed missing...']);
   
   %also write to the workspace
   assignin('base','codingArray',u);
   
end

%-----------------
%display a frame in the axis
function displayFrame(currentFrame)
global CURRENT_VIDEO;

%display the frame
image(read(CURRENT_VIDEO,currentFrame),'Parent',gca);
axis off

%-----------------
%a function to receive the gaze data, now only triggered by timer
function logGazeLocation()
%get the current frame from the slider position
h=findobj(gcf,'Style','slider');
currentFrame=round(get(h,'Value'));

%get the currently selected radio button
bg=findobj(gcf,'Type','uipanel');
h=get(bg,'SelectedObject');
s=get(h,'String');
ia=str2double(get(h,'Tag'));
   
%write a helpful message to the console
disp(['Gaze recorded on ',s,' at frame ',int2str(currentFrame)]);

%use the area index to add to the gaze vector
gazeVector=get(gcf,'UserData');
gazeVector(currentFrame,2:size(gazeVector,2))=0;
gazeVector(currentFrame,ia+1)=1;
set(gcf,'UserData',gazeVector);

%-----------------
%display the next frame, and move the slider accordingly
function advanceFrame()

%get the current frame from the slider position
h=findobj(gcf,'Style','slider');
currentFrame=get(h,'Value');

%check we're within the video
if currentFrame < get(h,'Max');
    %display the next frame
    displayFrame(currentFrame+1)
    %advance the slider
    set(h,'Value',currentFrame+1)
end


%------------------
%when user tries to close, check that they're sure and they've saved data
function closeMainFigure(hObject, eventdata)
q=questdlg('Are you sure you want to exit? (Have you saved gaze data?)');
if strcmp(q,'Yes')
    global t;
    delete(t);
    delete(hObject);
end

%-----------------
%when user changes the slider, change the current frame
function advanceSlider(hObject, eventdata, handles)
f=round(get(hObject,'Value'));
displayFrame(f);


%-----------------
%when user presses a key....
function iaKeyPress(hObject, eventdata, handles)
global t

    bg=findobj(gcf,'Type','uipanel');
    h=findobj(gcf,'Parent',bg);
    b=strcmp(get(h(1),'Enable'),'on');

    %is it one we care about?
    k=str2double(eventdata.Character);
    if k>0 && k<=length(h) && b
        
        %change the selected radio button accordingly
        h=findobj(gcf,'Tag',int2str(k));
        set(h,'Value',1);
        
        %if we're not playing, advance frame and log
        p=strcmp(get(t,'Running'),'on');
        if ~p
           logGazeLocation();
           disp('advancing..');
           advanceFrame(); 
        end
    end


%------------------
%when user clicks the start button, start advancing
function controlMovie(hObject, eventdata, handles)
%use a global playing variable
global t;

%toggle it
p=strcmp(get(t,'Running'),'on');
if p
    stop(t);
    set(hObject,'String','Play movie');
else
    start(t);
    set(hObject,'String','Pause movie');
end

%whenever the timer executes...
function timercallback(obj, event)
%log the current ia
logGazeLocation();
%advance the frame
disp('advancing..');
advanceFrame();

