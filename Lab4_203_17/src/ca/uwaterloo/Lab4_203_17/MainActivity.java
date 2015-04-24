package ca.uwaterloo.Lab4_203_17;
 
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
 




import ca.uwaterloo.Lab4_203_17.LineGraphView;
import ca.uwaterloo.Lab4_203_17.MainActivity.PlaceholderFragment.Tracker;
import ca.uwaterloo.Lab4_203_17.MapLoader;
import ca.uwaterloo.Lab4_203_17.MapView;
import ca.uwaterloo.Lab4_203_17.NavigationalMap;
import ca.uwaterloo.Lab4_203_17.PositionListener;
import ca.uwaterloo.Lab4_203_17.VectorUtils;
import android.app.Activity;
import android.graphics.PointF;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
 
class AccelerometerSensorEventListener implements SensorEventListener {
	LineGraphView output;
	TextView instruction1;
	TextView instruction2;
	TextView stepsW;
	TextView stepsE;
	TextView stepsN;
	TextView orientationZ;
	float lastZ;
	static int stepNorth=0;
	static int stepWest=0;
	static int stepSouth=0;
	static int stepEast=0;
	double azimuth;
	double previousazimuth;
	float[] magVal=new float[3];
	float[] accelVal=new float [3];
	boolean notSouth=true;
	static MapView mv;
	static NavigationalMap navMap;
	static VectorUtils vu;
	static Tracker track;
	static boolean p1passed;
	static boolean p2passed;
	
	public AccelerometerSensorEventListener(TextView instruction1, TextView W, TextView E, TextView N, TextView azimuth,LineGraphView graph, TextView instruction2){
		output = graph;
		this.instruction1=instruction1;
		this.instruction2=instruction2;
		stepsE=E;
		stepsW=W;
		stepsN=N;
		orientationZ=azimuth;
	}
	public void onAccuracyChanged(Sensor s, int i) {}
	public void onSensorChanged(SensorEvent se) {
		if (se.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			accelVal=se.values.clone();
		}
		if (se.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
			magVal=se.values.clone();
		}
		
		//Compass
		if (accelVal!=null && magVal!=null){
            float[] R = new float[16];
            float[] I = new float[16];
	        boolean success =SensorManager.getRotationMatrix(R, I, accelVal, magVal);
	        if (success){

	        	float[] orientation=new float[3];
	        	SensorManager.getOrientation(R, orientation);
	        	azimuth=orientation[0]*180/Math.PI;
	        	orientationZ.setText("Azimuth: "+ azimuth);
	        }
		}
		
		//Smoothing Acceleration
		float[] smoothedData = new float[3];
		smoothedData[0] += (accelVal[0] - smoothedData[0]) / 800f;
		smoothedData[1] += (accelVal[1] - smoothedData[1]) / 800f;
		smoothedData[2] += (accelVal[2] - smoothedData[2]) / 290f;
		
		output.addPoint(smoothedData); 
		
		//Step Detection Algorithm
		if ( smoothedData[2] <0.02 && Math.abs(smoothedData[2]-lastZ) > 0.006 && Math.abs(smoothedData[2]-lastZ) < 0.01){
			if (previousazimuth >= 150 || previousazimuth <=-150) notSouth=false;
			else {notSouth=true;}
			
			//North
			if (azimuth >=-45 && azimuth <=45 && notSouth==true){
						stepNorth++;
						stepsN.setText("Steps North: " + stepNorth);
			}
			
			//South
			if ((azimuth >=135 || azimuth <=-135) && notSouth==false ){
				stepNorth--;
				stepsN.setText("Steps North: " + stepNorth);
			}
			
			//West
			if (azimuth >-135 && azimuth <-45 && notSouth==true ){
				stepEast--;
				stepsE.setText("Steps East: " + stepEast);
				
			}
			
			//East
			if (azimuth >45 && azimuth <135 && notSouth==true ){
				stepEast++;
				stepsE.setText("Steps East: " + stepEast);
			}
			
			

            if (mv.getOriginPoint() != null
                            && mv.getDestinationPoint() != null) {
            		
            		instruction1.setText("");
            		instruction2.setText("");
                    mv.setUserPoint(mv.getOriginPoint().x
                                    + stepEast / 3f,
                                    mv.getOriginPoint().y - stepNorth
                                                    / 3f);
                    mv.setUserPath(track.determinePath(mv.getUserPoint(),
                            mv.getDestinationPoint(), 800, 550, 40, 40));
                    
                    if (track.calculateDistanceToEnd(mv.getUserPoint(),mv.getDestinationPoint())<=0.55){
                    	stepsW.setText("You have arrived!");
                    	instruction1.setText(null);
                    	instruction2.setText(null);
                    }
                    else {
                    	stepsW.setText("You are not there yet");
                    }
                    
                    PointF topcheck=new PointF(mv.getUserPoint().x,mv.getUserPoint().y);
            		PointF botcheck=new PointF(mv.getUserPoint().x+0.25f,mv.getUserPoint().y);
            		PointF leftcheck=new PointF(mv.getUserPoint().x,mv.getUserPoint().y-0.2f);
            		PointF rightcheck=new PointF(mv.getUserPoint().x,mv.getUserPoint().y+0.2f);
            		if (track.checkNearWalls(mv.getUserPoint(), topcheck) == false || track.checkNearWalls(mv.getUserPoint(), botcheck) == false 
            				|| track.checkNearWalls(mv.getUserPoint(), leftcheck) == false || track.checkNearWalls(mv.getUserPoint(), rightcheck) == false){
            			stepsW.setText("Undo last step taken");
            		}
            }
			
		}
		lastZ=smoothedData[2];
		previousazimuth=azimuth;
			
	}		
}









public class MainActivity extends ActionBarActivity {

    // NavigationalMap navmap;
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		
		}
	
	}

	


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	
	
	

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment implements PositionListener{

		public PlaceholderFragment() {
		}
		
		@Override
		public void onCreateContextMenu ( ContextMenu menu , View v , ContextMenuInfo menuInfo ) {
		super . onCreateContextMenu ( menu , v , menuInfo );
		AccelerometerSensorEventListener.mv . onCreateContextMenu ( menu , v , menuInfo );
		}
		@Override
		public boolean onContextItemSelected ( MenuItem item ) {
		return super . onContextItemSelected ( item ) || AccelerometerSensorEventListener.mv . onContextItemSelected ( item );
		}
		

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			
			

			final LinearLayout lmain = (LinearLayout) rootView.findViewById(R.id.label0);
            lmain.setOrientation(LinearLayout.VERTICAL);
			//TextViews
			
			//Accelerometer
			
			TextView tv2 = new TextView(rootView.getContext());
			tv2.setText("Accelerometer (m/s^2)");
			lmain.addView(tv2);
			
		
			

				
					
				
			
			lmain.setOrientation(LinearLayout.VERTICAL);
			
			LineGraphView graph;
			graph = new LineGraphView(rootView.getContext(),
					100,
					Arrays.asList("x", "y", "z"));
				
					lmain.addView(graph);
					graph.setVisibility(View.VISIBLE);


			                

					

			
	
					
			//Steps North
			final TextView tv7 = new TextView(rootView.getContext());
			tv7.setText("Steps North: 0");
			lmain.addView(tv7);
			
	
			
			//Steps East
			final TextView tv5 = new TextView(rootView.getContext());
			tv5.setText("Steps East: 0");
			lmain.addView(tv5);
		    
			//Guideline
			final TextView tv3 = new TextView(rootView.getContext());
			tv3.setText("Direction GuideLine: \nAzimuth=0 is North \nAzimuth=90 is East \nAzimuth=180 is South \nAzimuth=-90 is West");
			lmain.addView(tv3);
			
			TextView tv8 = new TextView(rootView.getContext());
			tv8.setText("Azimuth: 0");
			lmain.addView(tv8);


			final TextView tv6 = new TextView(rootView.getContext());
			tv6.setText("Instructions ");
			lmain.addView(tv6);
			
			final TextView tv9 = new TextView(rootView.getContext());
			lmain.addView(tv9);
			
			//Destination Arriver Checker
			final TextView tv4 = new TextView(rootView.getContext());
			tv4.setText("You are not there yet");
			lmain.addView(tv4);
			
			
			//Map View
			AccelerometerSensorEventListener.mv = new MapView (rootView.getContext() , 800, 550, 40, 40);
			registerForContextMenu (AccelerometerSensorEventListener. mv );
			NavigationalMap map = MapLoader . loadMap (rootView.getContext().getExternalFilesDir ( null ) ,
					"Lab-room-peninsula.svg");

			AccelerometerSensorEventListener.mv . setMap ( map );
			lmain.addView(AccelerometerSensorEventListener.mv);
			AccelerometerSensorEventListener.mv.addListener(this);		
			AccelerometerSensorEventListener.navMap= new NavigationalMap();
			AccelerometerSensorEventListener.track=new Tracker(AccelerometerSensorEventListener.mv
					,map,tv6,tv9);
			
			//Reset Step Counter Button
			Button reset = (Button)rootView.findViewById(R.id.button1);
			
			reset.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					AccelerometerSensorEventListener.stepNorth = 0;
					AccelerometerSensorEventListener.stepWest = 0;
					AccelerometerSensorEventListener.stepSouth = 0;
					AccelerometerSensorEventListener.stepEast = 0;
					tv5.setText("Steps East: " +  AccelerometerSensorEventListener.stepEast );
					tv7.setText("Steps North: " + AccelerometerSensorEventListener.stepNorth);
					tv4.setText("Steps West: " + AccelerometerSensorEventListener.stepWest);

					AccelerometerSensorEventListener.mv.setUserPoint(AccelerometerSensorEventListener.mv.getOriginPoint().x,
							AccelerometerSensorEventListener.mv.getOriginPoint().y);

				}
			});		
			
					
					
			
			//Sensors 
			SensorManager sensorManager = (SensorManager)
					rootView.getContext().getSystemService(SENSOR_SERVICE);
			
			Sensor accelSensor =
					sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
			Sensor magneticSensor =
					sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
			
			AccelerometerSensorEventListener a = new AccelerometerSensorEventListener(tv6,tv4,tv5,tv7,tv8,graph,tv9);
			sensorManager.registerListener(a, magneticSensor,
					SensorManager.SENSOR_DELAY_NORMAL);
			sensorManager.registerListener(a, accelSensor,
					SensorManager.SENSOR_DELAY_NORMAL);
		
		

			
			return rootView;
		}

		@Override
		public void originChanged(MapView source, PointF loc) {
			source.setOriginPoint(loc);
            source.setUserPoint(loc);
            AccelerometerSensorEventListener.p1passed=false;
            AccelerometerSensorEventListener.p2passed=false;
		}

		@Override
		public void destinationChanged(MapView source, PointF dest) {
            source.setDestinationPoint(dest);
            source.setUserPath(AccelerometerSensorEventListener.track.determinePath(source.getOriginPoint(),
                           source.getDestinationPoint(), 800, 550, 40, 40));
            AccelerometerSensorEventListener.p1passed=false;
            AccelerometerSensorEventListener.p2passed=false;
			
		}
		
		
		
		public class Tracker {
			MapView mv;
			NavigationalMap path;
			int dotsPerMeter;
			TextView Y;
			TextView X;
			float dx, dy;
            List<Node> dict;
            List<Node> blacklist;
            float ycounterup=0;
            float ycounterdown=0;
            float yendup=0;
            float yenddown=0;
            float xstartleft=0;
            float xstartright=0;
            float xendleft=0;
            float xendright=0;
            float commony=0;
            float commonx=0;
            ArrayList<Float> ystartRange=new ArrayList<Float>();
            ArrayList<Float> yendRange=new ArrayList<Float>();;
            ArrayList<Float> yRange=new ArrayList<Float>();
            ArrayList<Float> xstartRange=new ArrayList<Float>();
            ArrayList<Float> xendRange=new ArrayList<Float>();;
            ArrayList<Float> xRange=new ArrayList<Float>();
            
			public Tracker (MapView mv, NavigationalMap path, TextView tv6, TextView tv9){
				this.mv=mv;
				this.path=path;
				dotsPerMeter=3;
				Y=tv6;
				X=tv9;
			}
			
			
			
			public ArrayList<PointF> determinePath(PointF start, PointF end,
                    float sizeX, float sizeY, float xScale, float yScale){
				
				ArrayList<PointF> a = new ArrayList<PointF>();
				if (path.calculateIntersections(start, end).isEmpty()){
					a.add(start);
					a.add(end);
					if (start.y > end.y && Math.round(this.getDy(start,end))!=0 )Y.setText("Instruction: " + "North " +  Math.abs(Math.round(this.getDy(start,end))*3) + " steps");
					if (start.y < end.y && Math.round(this.getDy(start,end))!=0)Y.setText("Instruction: " + "South " +  Math.abs(Math.round(this.getDy(start,end)*3)) + " steps");
					if (start.x < end.x && Math.round(this.getDx(start,end))!=0)X.setText("East " +  Math.abs(Math.round(this.getDx(start,end)*3)) + " steps");
					if (start.x > end.x && Math.round(this.getDy(start,end))!=0)X.setText("West " +  Math.abs(Math.round(this.getDx(start,end)*3)) + " steps");
					
				}
				else {
					if (WallSearchY(start, end)){
					PointF p1=new PointF(start.x,commony);
					PointF p2= new PointF(end.x,commony);
					a.add(start);
					a.add(p1);
					a.add(p2);
					a.add(end);
					
					if (calculateDistanceToEnd(start,p1) <= 0.55) AccelerometerSensorEventListener.p1passed=true;
					if (calculateDistanceToEnd(start,p2) <= 0.55) AccelerometerSensorEventListener.p2passed=true;
					if (AccelerometerSensorEventListener.p1passed==false){
						if (start.y > p1.y && Math.round(this.getDy(start,p1))!=0 )Y.setText("Instruction: " + "North " +  Math.abs(Math.round(this.getDy(start,p1))*3) + " steps");
						if (start.y < p1.y && Math.round(this.getDy(start,p1))!=0)Y.setText("Instruction: " + "South " +  Math.abs(Math.round(this.getDy(start,p1)*3)) + " steps");
						if (start.x < p1.x && Math.round(this.getDx(start,p1))!=0)X.setText("East " +  Math.abs(Math.round(this.getDx(start,p1)*3)) + " steps");
						if (start.x > p1.x && Math.round(this.getDy(start,p1))!=0)X.setText("West " +  Math.abs(Math.round(this.getDx(start,p1)*3)) + " steps");
					}
					if (AccelerometerSensorEventListener.p1passed==true && AccelerometerSensorEventListener.p2passed==false){
						if (start.y > p2.y && Math.round(this.getDy(start,p2))!=0 )Y.setText("Instruction: " + "North " +  Math.abs(Math.round(this.getDy(start,p2))*3) + " steps");
						if (start.y < p2.y && Math.round(this.getDy(start,p2))!=0)Y.setText("Instruction: " + "South " +  Math.abs(Math.round(this.getDy(start,p2)*3)) + " steps");
						if (start.x < p2.x && Math.round(this.getDx(start,p2))!=0)X.setText("East " +  Math.abs(Math.round(this.getDx(start,p2)*3)) + " steps");
						if (start.x > p2.x && Math.round(this.getDy(start,p2))!=0)X.setText("West " +  Math.abs(Math.round(this.getDx(start,p2)*3)) + " steps");
					}
					//Y.setText("Y Coord: " + a.size());
					
					}
					else{
						
					}
				}
				ystartRange.clear();
				yendRange.clear();
				yenddown=0;
				yendup=0;
				ycounterdown=0;
				ycounterup=0;
				return a;
		
			}
			
			public boolean checkNearWalls(PointF user,PointF walls){
				return path.calculateIntersections(user, walls ).isEmpty();
			}
			
			public double calculateDistanceToEnd(PointF start, PointF end){
				double dx=Double.parseDouble(Float.toString(end.x-start.x));
				double dy=Double.parseDouble(Float.toString(end.y-start.y));
				
				
				return Math.abs(Math.sqrt(Math.pow(dx,2)+Math.pow(dy, 2)));
				
			}
			
			public double getDy(PointF start,PointF end){
				double dx=Double.parseDouble(Float.toString(end.x-start.x));
				double dy=Double.parseDouble(Float.toString(end.y-start.y));
				return dy;
			}
			
			public double getDx(PointF start,PointF end){
				double dx=Double.parseDouble(Float.toString(end.x-start.x));
				double dy=Double.parseDouble(Float.toString(end.y-start.y));
				return dx;
			}
			
			public boolean WallSearchY(PointF start, PointF end){
				//Find the range of acceptable y values on the start
				PointF topfinder=start;
				//start up
				while (path.calculateIntersections(start,topfinder).isEmpty()){
					ystartRange.add(topfinder.y);
					ycounterup-=0.25;
					topfinder=new PointF(start.x,start.y+ycounterup);
					
				}
				
				//start down
				topfinder=start;
				while (path.calculateIntersections(start,topfinder).isEmpty()){
					ystartRange.add(topfinder.y);
					ycounterdown+=0.25;
					topfinder=new PointF(start.x,start.y+ycounterdown);
					
				}
				
				//////////////////////////////////////////////
				//Find acceptable end y values
				topfinder=end;
				while (path.calculateIntersections(end,topfinder).isEmpty()){
					yendRange.add(topfinder.y);
					yendup-=0.25;
					topfinder=new PointF(end.x,end.y+yendup);
					
				}
				
				topfinder=end;
				while (path.calculateIntersections(end,topfinder).isEmpty()){
					yendRange.add(topfinder.y);
					yenddown+=0.25;
					topfinder=new PointF(end.x,end.y+yenddown);
					
				}
				
				//Find similar y values
				for (int j=0;j<ystartRange.size();j++){
					for (int k=0;k<yendRange.size();k++){
						if (Math.abs(ystartRange.get(j)-yendRange.get(k)) <=0.1  && Math.round(ystartRange.get(j))==Math.round(yendRange.get(k))){
							yRange.add(ystartRange.get(j));
						}
					}
				}
				
				if(yRange.isEmpty()==false){
					for (int i=0;i<yRange.size();i++){
						PointF yStartCoord=new PointF(start.x,yRange.get(i));
						PointF yEndCoord=new PointF(end.x,yRange.get(i));
						if (path.calculateIntersections(yStartCoord,yEndCoord).isEmpty()){
							commony=yRange.get(i);
							return true;
						}
					}
				}
				return false;
			}
			
			
			public boolean WallSearchX(PointF start, PointF end){
				//Find the range of acceptable y values on the start
				PointF leftfinder=start;
				//start left
				while (path.calculateIntersections(start,leftfinder).isEmpty()){
					xstartRange.add(leftfinder.x);
					xstartleft-=0.25;
					leftfinder=new PointF(start.x+xstartleft,start.y);
					
				}
				
				//start right
				leftfinder=start;
				while (path.calculateIntersections(start,leftfinder).isEmpty()){
					xstartRange.add(leftfinder.y);
					xstartright+=0.25;
					leftfinder=new PointF(start.x,start.y+ycounterdown);
					
				}
				
				//////////////////////////////////////////////
				//Find acceptable end y values
				leftfinder=end;
				while (path.calculateIntersections(end,leftfinder).isEmpty()){
					yendRange.add(leftfinder.y);
					yendup-=0.25;
					leftfinder=new PointF(end.x,end.y+yendup);
					
				}
				
				leftfinder=end;
				while (path.calculateIntersections(end,leftfinder).isEmpty()){
					yendRange.add(leftfinder.y);
					yenddown+=0.25;
					leftfinder=new PointF(end.x,end.y+yenddown);
					
				}
				
				//Find similar y values
				for (int j=0;j<ystartRange.size();j++){
					for (int k=0;k<yendRange.size();k++){
						if (Math.abs(ystartRange.get(j)-yendRange.get(k)) <=0.1  && Math.round(ystartRange.get(j))==Math.round(yendRange.get(k))){
							yRange.add(ystartRange.get(j));
						}
					}
				}
				
				if(xRange.isEmpty()==false){
					for (int i=0;i<xRange.size();i++){
						PointF yStartCoord=new PointF(start.x,xRange.get(i));
						PointF yEndCoord=new PointF(end.x,xRange.get(i));
						if (path.calculateIntersections(yStartCoord,yEndCoord).isEmpty()){
							commony=xRange.get(i);
							return true;
						}
					}
				}
				return false;
			}
		}
				
				
			}
			
			public class Node{
				PointF node;
				ArrayList<PointF> path;
				float cost;
				
				public Node (PointF node, ArrayList<PointF> path  ){
					this.node=node;
					this.path=path;
				}
				
				public float getCost(){
					return cost;
				}
				
				public PointF getPoint() {
                    return node;
				}
				
				private int LowestCost(List<Node> nodes) {
                    float minCost = 999999999;
                    int minIn = 0;
                    for (int i = 0; i < nodes.size(); i++) {
                            if (nodes.get(i).getCost() < minCost) {
                                    minCost = nodes.get(i).getCost();
                                    minIn = i;
                            }
                    }
                    return minIn;
				}
				
	
				
                public ArrayList<PointF> getPath() {
                    return path;
                }
				
			}

			
			
		
	}


