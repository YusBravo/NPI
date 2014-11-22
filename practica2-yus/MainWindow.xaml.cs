//------------------------------------------------------------------------------
// <copyright file="MainWindow.xaml.cs" company="Microsoft">
//     Copyright (c) Microsoft Corporation.  All rights reserved.
// </copyright>
//------------------------------------------------------------------------------

namespace Microsoft.Samples.Kinect.SkeletonBasics
{
    using System.IO;
    using System.Windows;
    using System.Windows.Media;
    using Microsoft.Kinect;
    using System;    

    /// <summary>
    /// Interaction logic for MainWindow.xaml
    /// </summary>
    public partial class MainWindow : Window
    {
        /// <summary>
        /// Width of output drawing
        /// </summary>
        private const float RenderWidth = 640.0f;

        /// <summary>
        /// Height of our output drawing
        /// </summary>
        private const float RenderHeight = 480.0f;

        /// <summary>
        /// Thickness of drawn joint lines
        /// </summary>
        private const double JointThickness = 3;

        /// <summary>
        /// Thickness of body center ellipse
        /// </summary>
        private const double BodyCenterThickness = 10;

        /// <summary>
        /// Thickness of clip edge rectangles
        /// </summary>
        private const double ClipBoundsThickness = 10;

        /// <summary>
        /// Brush used to draw skeleton center point
        /// </summary>
        private readonly Brush centerPointBrush = Brushes.Blue;

        /// <summary>
        /// Brush used for drawing joints that are currently tracked
        /// </summary>
        private readonly Brush trackedJointBrush = new SolidColorBrush(Color.FromArgb(255, 68, 192, 68));

        /// <summary>
        /// Brush used for drawing joints that are currently inferred
        /// </summary>        
        private readonly Brush inferredJointBrush = Brushes.Yellow;

        /// <summary>
        /// Pen used for drawing bones that are currently tracked
        /// </summary>
        private readonly Pen trackedBonePen = new Pen(Brushes.Green, 6);

        /// <summary>
        /// Pen used for drawing bones when move is in course from initial pos to OK
        /// </summary>
        private readonly Pen inCourseBonePen = new Pen(Brushes.Yellow, 6);

        /// <summary>
        /// Pen used for drawing bones when an error move
        /// </summary>
        private readonly Pen errorBonePen = new Pen(Brushes.Red, 6);

        /// <summary>
        /// Pen used for drawing bones that are currently inferred
        /// </summary>        
        private readonly Pen inferredBonePen = new Pen(Brushes.Gray, 1);

        /// <summary>
        /// float used for establish distance
        /// </summary> 
        private float distanceMov27 = 0.4f;

        /// <summary>
        /// enum uses for order state movement
        /// </summary> 
        public enum StateMov
        {
            INITIAL, LETMOVE, DONEMOV
        };

        public enum ArmRorL
        {
            RIGHT,LEFT
        };

        private ArmRorL armExercise = ArmRorL.RIGHT;
        private ArmRorL armRepose = ArmRorL.LEFT;

        public enum PartBody
        {
            ARMREPOSE, ARMEXERCISE, BODY, FOOTS 
        }

        private bool armReposeOK = false;
        private bool armExerciseOK = false;
        private bool bodyOK = false;
        private bool feetOK = false;

        Point[] pointsBaseArmExercise = new Point[4];

        public enum StateTracePoints
        {
            INITIAL, ONEPASSED, COMPLETE
        };


        StateTracePoints[] jointsBaseArmExerciseState = { StateTracePoints.INITIAL, 
                                                          StateTracePoints.ONEPASSED, 
                                                          StateTracePoints.COMPLETE, 
                                                          StateTracePoints.INITIAL 
                                                        };

        /// <summary>
        /// StateMov used for know actual state mov
        /// </summary> 
        private StateMov stateMov = StateMov.INITIAL;

        /// <summary>
        /// Active Kinect sensor
        /// </summary>
        private KinectSensor sensor;

        /// <summary>
        /// Drawing group for skeleton rendering output
        /// </summary>
        private DrawingGroup drawingGroup;

        /// <summary>
        /// Drawing image that we will display
        /// </summary>
        private DrawingImage imageSource;

        /// <summary>
        /// Initializes a new instance of the MainWindow class.
        /// </summary>
        public MainWindow()
        {
            InitializeComponent(); 
        }

        /// <summary>
        /// Draws indicators to show which edges are clipping skeleton data
        /// </summary>
        /// <param name="skeleton">skeleton to draw clipping information for</param>
        /// <param name="drawingContext">drawing context to draw to</param>
        private static void RenderClippedEdges(Skeleton skeleton, DrawingContext drawingContext)
        {
            if (skeleton.ClippedEdges.HasFlag(FrameEdges.Bottom))
            {
                drawingContext.DrawRectangle(
                    Brushes.Red,
                    null,
                    new Rect(0, RenderHeight - ClipBoundsThickness, RenderWidth, ClipBoundsThickness));
            }

            if (skeleton.ClippedEdges.HasFlag(FrameEdges.Top))
            {
                drawingContext.DrawRectangle(
                    Brushes.Red,
                    null,
                    new Rect(0, 0, RenderWidth, ClipBoundsThickness));
            }

            if (skeleton.ClippedEdges.HasFlag(FrameEdges.Left))
            {
                drawingContext.DrawRectangle(
                    Brushes.Red,
                    null,
                    new Rect(0, 0, ClipBoundsThickness, RenderHeight));
            }

            if (skeleton.ClippedEdges.HasFlag(FrameEdges.Right))
            {
                drawingContext.DrawRectangle(
                    Brushes.Red,
                    null,
                    new Rect(RenderWidth - ClipBoundsThickness, 0, ClipBoundsThickness, RenderHeight));
            }
        }

        /// <summary>
        /// Execute startup tasks
        /// </summary>
        /// <param name="sender">object sending the event</param>
        /// <param name="e">event arguments</param>
        private void WindowLoaded(object sender, RoutedEventArgs e)
        {
            // Create the drawing group we'll use for drawing
            this.drawingGroup = new DrawingGroup();

            // Create an image source that we can use in our image control
            this.imageSource = new DrawingImage(this.drawingGroup);

            // Display the drawing using our image control
            Image.Source = this.imageSource;

            // Look through all sensors and start the first connected one.
            // This requires that a Kinect is connected at the time of app startup.
            // To make your app robust against plug/unplug, 
            // it is recommended to use KinectSensorChooser provided in Microsoft.Kinect.Toolkit (See components in Toolkit Browser).
            foreach (var potentialSensor in KinectSensor.KinectSensors)
            {
                if (potentialSensor.Status == KinectStatus.Connected)
                {
                    this.sensor = potentialSensor;
                    break;
                }
            }

            if (null != this.sensor)
            {
                // Turn on the skeleton stream to receive skeleton frames
                this.sensor.SkeletonStream.Enable();

                // Add an event handler to be called whenever there is new color frame data
                this.sensor.SkeletonFrameReady += this.SensorSkeletonFrameReady;

                // Start the sensor!
                try
                {
                    this.sensor.Start();
                }
                catch (IOException)
                {
                    this.sensor = null;
                }
            }

            if (null == this.sensor)
            {
                this.statusBarText.Text = Properties.Resources.NoKinectReady;
            }
        }

        /// <summary>
        /// Execute shutdown tasks
        /// </summary>
        /// <param name="sender">object sending the event</param>
        /// <param name="e">event arguments</param>
        private void WindowClosing(object sender, System.ComponentModel.CancelEventArgs e)
        {
            if (null != this.sensor)
            {
                this.sensor.Stop();
            }
        }

        /// <summary>
        /// Event handler for Kinect sensor's SkeletonFrameReady event
        /// </summary>
        /// <param name="sender">object sending the event</param>
        /// <param name="e">event arguments</param>
        private void SensorSkeletonFrameReady(object sender, SkeletonFrameReadyEventArgs e)
        {
            Skeleton[] skeletons = new Skeleton[0];

            using (SkeletonFrame skeletonFrame = e.OpenSkeletonFrame())
            {
                if (skeletonFrame != null)
                {
                    skeletons = new Skeleton[skeletonFrame.SkeletonArrayLength];
                    skeletonFrame.CopySkeletonDataTo(skeletons);
                }
            }

            using (DrawingContext dc = this.drawingGroup.Open())
            {
                // Draw a transparent background to set the render size
                dc.DrawRectangle(Brushes.Black, null, new Rect(0.0, 0.0, RenderWidth, RenderHeight));

                if (skeletons.Length != 0)
                {
                    foreach (Skeleton skel in skeletons)
                    {
                        RenderClippedEdges(skel, dc);

                        if (skel.TrackingState == SkeletonTrackingState.Tracked)
                        {
                            this.DrawBonesAndJoints(skel, dc);
                        }
                        else if (skel.TrackingState == SkeletonTrackingState.PositionOnly)
                        {
                            dc.DrawEllipse(
                            this.centerPointBrush,
                            null,
                            this.SkeletonPointToScreen(skel.Position),
                            BodyCenterThickness,
                            BodyCenterThickness);                            
                        }
                    }
                }

                // prevent drawing outside of our render area
                this.drawingGroup.ClipGeometry = new RectangleGeometry(new Rect(0.0, 0.0, RenderWidth, RenderHeight));
                
                if (stateMov == StateMov.LETMOVE)
                {
                    this.DrawTracePoints(dc);
                }
            }
        }

        
        /// <summary>
        /// Draws a skeleton's bones and joints
        /// </summary>
        /// <param name="skeleton">skeleton to draw</param>
        /// <param name="drawingContext">drawing context to draw to</param>
        private void DrawBonesAndJoints(Skeleton skeleton, DrawingContext drawingContext)
        {
            
            if (isMove27(skeleton, distanceMov27))
            {
                switch (stateMov)
                {
                    case StateMov.INITIAL:
                        stateMov = StateMov.LETMOVE;
                        break;
                    case StateMov.LETMOVE:
                        //stateMov = StateMov.DONEMOV;
                        break;
                }
            }
            else
            {
                if (stateMov == StateMov.DONEMOV)
                {
                    stateMov = StateMov.INITIAL;
                }
            }


            // Render Torso
            this.DrawBone(skeleton, drawingContext, JointType.Head, JointType.ShoulderCenter,bodyOK);
            this.DrawBone(skeleton, drawingContext, JointType.ShoulderCenter, JointType.ShoulderLeft, bodyOK);
            this.DrawBone(skeleton, drawingContext, JointType.ShoulderCenter, JointType.ShoulderRight, bodyOK);
            this.DrawBone(skeleton, drawingContext, JointType.ShoulderCenter, JointType.Spine, bodyOK);
            this.DrawBone(skeleton, drawingContext, JointType.Spine, JointType.HipCenter, bodyOK);
            this.DrawBone(skeleton, drawingContext, JointType.HipCenter, JointType.HipLeft, bodyOK);
            this.DrawBone(skeleton, drawingContext, JointType.HipCenter, JointType.HipRight, bodyOK);  
            
            if (armExercise == ArmRorL.RIGHT)
            {
                // Left Arm
                this.DrawBone(skeleton, drawingContext, JointType.ShoulderLeft, JointType.ElbowLeft,armReposeOK);
                this.DrawBone(skeleton, drawingContext, JointType.ElbowLeft, JointType.WristLeft, armReposeOK);
                this.DrawBone(skeleton, drawingContext, JointType.WristLeft, JointType.HandLeft, armReposeOK);
                // Right Arm
                this.DrawBone(skeleton, drawingContext, JointType.ShoulderRight, JointType.ElbowRight,armExerciseOK);
                this.DrawBone(skeleton, drawingContext, JointType.ElbowRight, JointType.WristRight, armExerciseOK);
                this.DrawBone(skeleton, drawingContext, JointType.WristRight, JointType.HandRight, armExerciseOK);
            }
            else
            {
                // Left Arm
                this.DrawBone(skeleton, drawingContext, JointType.ShoulderLeft, JointType.ElbowLeft, armExerciseOK);
                this.DrawBone(skeleton, drawingContext, JointType.ElbowLeft, JointType.WristLeft, armExerciseOK);
                this.DrawBone(skeleton, drawingContext, JointType.WristLeft, JointType.HandLeft, armExerciseOK);
                // Right Arm
                this.DrawBone(skeleton, drawingContext, JointType.ShoulderRight, JointType.ElbowRight, armReposeOK);
                this.DrawBone(skeleton, drawingContext, JointType.ElbowRight, JointType.WristRight, armReposeOK);
                this.DrawBone(skeleton, drawingContext, JointType.WristRight, JointType.HandRight, armReposeOK);
            }

            // Left Leg                        
            this.DrawBone(skeleton, drawingContext, JointType.HipLeft, JointType.KneeLeft, feetOK);
            this.DrawBone(skeleton, drawingContext, JointType.KneeLeft, JointType.AnkleLeft, feetOK);
            this.DrawBone(skeleton, drawingContext, JointType.AnkleLeft, JointType.FootLeft, feetOK);

            // Right Leg                       
            this.DrawBone(skeleton, drawingContext, JointType.HipRight, JointType.KneeRight, feetOK);
            this.DrawBone(skeleton, drawingContext, JointType.KneeRight, JointType.AnkleRight, feetOK);
            this.DrawBone(skeleton, drawingContext, JointType.AnkleRight, JointType.FootRight, feetOK);
 
            // Render Joints
            foreach (Joint joint in skeleton.Joints)
            {
                Brush drawBrush = null;

                if (joint.TrackingState == JointTrackingState.Tracked)
                {
                    drawBrush = this.trackedJointBrush;                    
                }
                else if (joint.TrackingState == JointTrackingState.Inferred)
                {
                    drawBrush = this.inferredJointBrush;                    
                }

                if (drawBrush != null)
                {
                    drawingContext.DrawEllipse(drawBrush, null, this.SkeletonPointToScreen(joint.Position), JointThickness, JointThickness);
                }
            }
                            
        }
   

        /// <summary>
        /// Maps a SkeletonPoint to lie within our render space and converts to Point
        /// </summary>
        /// <param name="skelpoint">point to map</param>
        /// <returns>mapped point</returns>
        private Point SkeletonPointToScreen(SkeletonPoint skelpoint)
        {
            // Convert point to depth space.  
            // We are not using depth directly, but we do want the points in our 640x480 output resolution.
            DepthImagePoint depthPoint = this.sensor.CoordinateMapper.MapSkeletonPointToDepthPoint(skelpoint, DepthImageFormat.Resolution640x480Fps30);
            return new Point(depthPoint.X, depthPoint.Y);
        }

        /// <summary>
        /// Draws a bone line between two joints
        /// </summary>
        /// <param name="skeleton">skeleton to draw bones from</param>
        /// <param name="drawingContext">drawing context to draw to</param>
        /// <param name="jointType0">joint to start drawing from</param>
        /// <param name="jointType1">joint to end drawing at</param>
        private void DrawBone(Skeleton skeleton, DrawingContext drawingContext, JointType jointType0, JointType jointType1, bool isOK)
        {
            Joint joint0 = skeleton.Joints[jointType0];
            Joint joint1 = skeleton.Joints[jointType1];

            // If we can't find either of these joints, exit
            if (joint0.TrackingState == JointTrackingState.NotTracked ||
                joint1.TrackingState == JointTrackingState.NotTracked)
            {
                return;
            }

            // Don't draw if both points are inferred
            if (joint0.TrackingState == JointTrackingState.Inferred &&
                joint1.TrackingState == JointTrackingState.Inferred)
            {
                return;
            }

            // We assume all drawn bones are inferred unless BOTH joints are tracked           
            Pen drawPen = this.inferredBonePen;
            if (joint0.TrackingState == JointTrackingState.Tracked && joint1.TrackingState == JointTrackingState.Tracked)
            {                
                switch (stateMov)
                {
                    case StateMov.INITIAL:
                        if (!isOK)
                        {
                            drawPen = this.errorBonePen;
                        }
                        else
                        {
                            drawPen = this.inCourseBonePen;
                        }
                        break;
                    case StateMov.LETMOVE:
                        drawPen = this.inCourseBonePen;
                        break;
                    case StateMov.DONEMOV:
                        drawPen = this.trackedBonePen;
                        break;
                }                                                  
            }
      
            drawingContext.DrawLine(drawPen, this.SkeletonPointToScreen(joint0.Position), this.SkeletonPointToScreen(joint1.Position));
        }

        /// <summary>
        /// Handles the checking or unchecking of the seated mode combo box
        /// </summary>
        /// <param name="sender">object sending the event</param>
        /// <param name="e">event arguments</param>
        private void CheckBoxSeatedModeChanged(object sender, RoutedEventArgs e)
        {
            if (null != this.sensor)
            {
                if (this.checkBoxSeatedMode.IsChecked.GetValueOrDefault())
                {
                    this.sensor.SkeletonStream.TrackingMode = SkeletonTrackingMode.Seated;
                }
                else
                {
                    this.sensor.SkeletonStream.TrackingMode = SkeletonTrackingMode.Default;
                }
            }
        }

        /// <summary>
        /// Return if skeleton is in mov27 OK - José Francisco Bravo Sänchez
        /// </summary>
        /// <param name="skeleton">skeleton to check</param>
        /// <param name="distance">input data. Distance to move the foot</param>        
        private bool isMove27(Skeleton skeleton, float distance)
        {
            
            bool check = false;            
            float initialDistanceBetweenKnees = 0.05f;            
            float initialDistanceBetweenAnkles = 0.3f;
            float initialDistanceHipAnkles = 0.1f;
            float distanceHipKneeLeft = 0.1f;
            float distanceOnTheFloor = 0.2f;
            Joint ankleLeft = skeleton.Joints[JointType.AnkleLeft],
                  ankleRight = skeleton.Joints[JointType.AnkleRight],
                  kneeLeft = skeleton.Joints[JointType.KneeLeft],
                  kneeRight = skeleton.Joints[JointType.KneeRight],
                  hipCenter = skeleton.Joints[JointType.HipCenter];                  

            switch (stateMov)
            {
                case StateMov.INITIAL:
                   /* if ((ankleRight.Position.X - ankleLeft.Position.X) < initialDistanceBetweenAnkles && //X Distance between ankles
                        (Math.Abs(kneeLeft.Position.Y - kneeRight.Position.Y) < initialDistanceBetweenKnees) && //Y Distance between knees
                        (Math.Abs(kneeLeft.Position.Z - kneeRight.Position.Z) < initialDistanceBetweenKnees) && //Z Distance between knees
                        ((Math.Abs(hipCenter.Position.X - ankleRight.Position.X) - Math.Abs(hipCenter.Position.X - ankleLeft.Position.X)) < initialDistanceHipAnkles)) //X Distance between kness - hip center
                    {
                        check = true;
                    
                    }*/
                    if ((IsAlignedBody(skeleton) && AreArmStraight(skeleton) && AreArm90grades(skeleton) && AreFeetSeparate(skeleton))){
                        check = true;
                        stateMov = StateMov.LETMOVE;
                        CalculateTracePoints(skeleton);
                    }
                    break;
                case StateMov.LETMOVE:
                break;
                case StateMov.DONEMOV:
                    if ((IsAlignedBody(skeleton) && AreArmDoneMov(skeleton) && AreArm90grades(skeleton) && AreFeetSeparate(skeleton)))
                    {
                        check = true;
                    }
                   /* if ((ankleLeft.Position.Z - ankleRight.Position.Z) > distance && //Z Distance between ankles (input method parameter)
                        (kneeRight.Position.Y - kneeLeft.Position.Y) < distanceOnTheFloor &&     // Y distance between knees (on the floor)                  
                        (ankleRight.Position.X - ankleLeft.Position.X) < initialDistanceBetweenAnkles && // X distance betwees ankles.
                        (Math.Abs(hipCenter.Position.X - ankleLeft.Position.X) < distanceHipKneeLeft)) //X Distance between kness - hip center
                        
                    {
                        check = true;
                    }*/                        
                    break;
            }

            textBoxLog.Clear();
            textBoxLog.Text = "ArmRepose: "+armReposeOK+" ArmExercise: "+armExerciseOK+" Body: "+bodyOK+" Feet:"+feetOK;

            return check;
        }

        
    // boolean method that return true if body is completely aligned
        private bool IsAlignedBody(Skeleton received)
        {
            bool check = false;
            bodyOK = false;

            double HipCenterPosX = received.Joints[JointType.HipCenter].Position.X;
            double HipCenterPosY = received.Joints[JointType.HipCenter].Position.Y;
            double HipCenterPosZ = received.Joints[JointType.HipCenter].Position.Z;

            double ShoulCenterPosX = received.Joints[JointType.ShoulderCenter].Position.X;
            double ShoulCenterPosY = received.Joints[JointType.ShoulderCenter].Position.Y;
            double ShoulCenterPosZ = received.Joints[JointType.ShoulderCenter].Position.Z;

            double HeadCenterPosX = received.Joints[JointType.Head].Position.X;
            double HeadCenterPosY = received.Joints[JointType.Head].Position.Y;
            double HeadCenterPosZ = received.Joints[JointType.Head].Position.Z;


            //Create method to verify if the center of the body is completely aligned
            //head with shoulder center and with hip center
            if (Math.Abs(HeadCenterPosX-ShoulCenterPosX)<=0.05 && Math.Abs(ShoulCenterPosX-HipCenterPosX)<=0.05)
            {                
                check = true;
                bodyOK = true;           
            }

            return check;
        }

        private bool AreArmDoneMov(Skeleton received){
            bool check = true;

            return check;
        }

        private bool AreArm90grades(Skeleton received)
        {
            bool check = false;
            armReposeOK = false;
            double shoulderX, elbowX, elbowY, elbowZ, wristY,wristZ;
            float distErrorSE = 0.1f;
            float distError = 0.1f;

            switch (armRepose)
            {
                case ArmRorL.RIGHT:
                    wristY = received.Joints[JointType.WristRight].Position.Y;
                    wristZ = received.Joints[JointType.WristRight].Position.Z;
                    elbowX = received.Joints[JointType.ElbowRight].Position.X;
                    elbowY = received.Joints[JointType.ElbowRight].Position.Y;
                    elbowZ = received.Joints[JointType.ElbowRight].Position.Z;
                    shoulderX = received.Joints[JointType.ShoulderRight].Position.X;
                break;
                default: //case LEFT
                    wristY = received.Joints[JointType.WristLeft].Position.Y;
                    wristZ = received.Joints[JointType.WristLeft].Position.Z;
                    elbowX = received.Joints[JointType.ElbowLeft].Position.X;
                    elbowY = received.Joints[JointType.ElbowLeft].Position.Y;
                    elbowZ = received.Joints[JointType.ElbowLeft].Position.Z;
                    shoulderX = received.Joints[JointType.ShoulderLeft].Position.X;
                break;
            }

            if (Math.Abs(shoulderX - elbowX) <= distErrorSE &&
                Math.Abs(elbowY - wristY) <= distError &&
                Math.Abs(elbowZ - wristZ) <= distError)
            {
                check = true;
                armReposeOK = true;
            }

            return check;            
        }

        private bool AreArmStraight(Skeleton received)
        {
            bool check = false;
            armExerciseOK = false;
            //caldulate admited error 5% that correspond to 9 degrees for each side
            double radian = (9 * Math.PI) / 180;
            double wristX, projectedWristX, distError;

            switch (armExercise)
            {
                case ArmRorL.RIGHT:
                    wristX = received.Joints[JointType.WristRight].Position.X;                    

                    double WriRPosX = received.Joints[JointType.WristRight].Position.X;
                    double WriRPosY = received.Joints[JointType.WristRight].Position.Y;
                   // double WriRPosZ = received.Joints[JointType.WristRight].Position.Z;
                    
                    double ShouRPosX = received.Joints[JointType.ShoulderRight].Position.X;
                    double ShouRPosY = received.Joints[JointType.ShoulderRight].Position.Y;
                    //double ShouRPosZ = received.Joints[JointType.ShoulderRight].Position.Z; 

                    projectedWristX = ShouRPosX;
                                        
                    double distShouLtoWristR = ShouRPosY - WriRPosY;           
                    distError = distShouLtoWristR * Math.Tan(radian);

                break;
                default: //case LEFT
                    wristX = received.Joints[JointType.WristLeft].Position.X;  

                    double WriLPosX = received.Joints[JointType.WristLeft].Position.X;
                    double WriLPosY = received.Joints[JointType.WristLeft].Position.Y;
                   // double WriLPosZ = received.Joints[JointType.WristLeft].Position.Z;

                    double ShouLPosX = received.Joints[JointType.ShoulderLeft].Position.X;
                    double ShouLPosY = received.Joints[JointType.ShoulderLeft].Position.Y;
                   // double ShouLPosZ = received.Joints[JointType.ShoulderLeft].Position.Z;

                    projectedWristX = ShouLPosX;
                    double ProjectedPointWristLY = WriLPosY;
                   
                    double distShouLtoWristL = ShouLPosY - WriLPosY;
                    distError = distShouLtoWristL * Math.Tan(radian);
                break;
            }

            if (Math.Abs(wristX - projectedWristX) <= distError)
            {
                check = true;
                armExerciseOK = true;
            }

            return check;
        }

        private bool AreArmInPosition()
        {
            return true;
        }

        //first position to be Tracked and Accepted
        private bool AreFeetTogether(Skeleton received)
        {
            bool check = false;
            feetOK = false;

            if (null != this.sensor)
            {
                foreach (Joint joint in received.Joints)
                {
                    if (joint.TrackingState == JointTrackingState.Tracked)
                    {//first verify if the body is alignet and arms are in a relaxed position

                        //{here verify if the feet are together
                        //use the same strategy that was used in the previous case of the arms in a  relaxed position
                        double HipCenterPosX = received.Joints[JointType.HipCenter].Position.X;
                        double HipCenterPosY = received.Joints[JointType.HipCenter].Position.Y;
                        double HipCenterPosZ = received.Joints[JointType.HipCenter].Position.Z;

                        //if left ankle is very close to right ankle then verify the rest of the skeleton points
                        //if (received.Joints[JointType.AnkleLeft].Equals(received.Joints[JointType.AnkleRight])) 
                        double AnkLPosX = received.Joints[JointType.AnkleLeft].Position.X;
                        double AnkLPosY = received.Joints[JointType.AnkleLeft].Position.Y;
                        double AnkLPosZ = received.Joints[JointType.AnkleLeft].Position.Z;

                        double AnkRPosX = received.Joints[JointType.AnkleRight].Position.X;
                        double AnkRPosY = received.Joints[JointType.AnkleRight].Position.Y;
                        double AnkRPosZ = received.Joints[JointType.AnkleRight].Position.Z;
                        //assume that the distance Y between HipCenter to each foot is the same
                        double distHiptoAnkleL = HipCenterPosY - AnkLPosY;
                        //caldulate admited error 5% that correspond to 9 degrees for each side
                        double radian1 = (4.5 * Math.PI) / 180;
                        double DistErrorL = distHiptoAnkleL * Math.Tan(radian1);
                        //determine of projected point from HIP CENTER to LEFT ANKLE and RIGHT and then assume error
                        double ProjectedPointFootLX = HipCenterPosX;
                        double ProjectedPointFootLY = AnkLPosY;
                        double ProjectedPointFootLZ = HipCenterPosZ;



                        // could variate AnkLposX and AnkLPosY
                        if (Math.Abs(AnkLPosX - ProjectedPointFootLX) <= DistErrorL && Math.Abs(AnkRPosX - ProjectedPointFootLX) <= DistErrorL)
                        {
                            check = true;
                            feetOK = true;
                        }                                         
                    }//CLOSE if (joint.TrackingState == JointTrackingState.Tracked)                    
                }//close foreach

            }//close if !null

            return check;
        }//close method AreFeetTogether



        //method for the second position feet separate between 60 degrees to be accepted
        private bool AreFeetSeparate(Skeleton received)
        {
            double gradesAnkleHipCenter = 30;
            bool check = false;
            feetOK = false;

        
                        //{//here verify if the feet are together
                        //use the same strategy that was used in the previous case of the arms in a  relaxed position
                        double HipCenterPosX = received.Joints[JointType.HipCenter].Position.X;
                        double HipCenterPosY = received.Joints[JointType.HipCenter].Position.Y;
                        double HipCenterPosZ = received.Joints[JointType.HipCenter].Position.Z;

                        //if left ankle is very close to right ankle then verify the rest of the skeleton points
                        //if (received.Joints[JointType.AnkleLeft].Equals(received.Joints[JointType.AnkleRight])) 
                        double AnkLPosX = received.Joints[JointType.AnkleLeft].Position.X;
                        double AnkLPosY = received.Joints[JointType.AnkleLeft].Position.Y;
                        double AnkLPosZ = received.Joints[JointType.AnkleLeft].Position.Z;

                        double AnkRPosX = received.Joints[JointType.AnkleRight].Position.X;
                        double AnkRPosY = received.Joints[JointType.AnkleRight].Position.Y;
                        double AnkRPosZ = received.Joints[JointType.AnkleRight].Position.Z;
                        //assume that the distance Y between HipCenter to each foot is the same
                        double distHiptoAnkleL = HipCenterPosY - AnkLPosY;
                        //caldulate admited error 5% that correspond to 9 degrees for each side
                        double radian1 = (4.5 * Math.PI) / 180;
                        double DistErrorL = distHiptoAnkleL * Math.Tan(radian1);
                        //determine of projected point from HIP CENTER to LEFT ANKLE and RIGHT and then assume error
                        double ProjectedPointFootLX = HipCenterPosX;
                        double ProjectedPointFootLY = AnkLPosY;
                        double ProjectedPointFootLZ = HipCenterPosZ;

                        double radian2 = (gradesAnkleHipCenter * Math.PI) / 180;
                        double DistSeparateFoot = distHiptoAnkleL * Math.Tan(radian2);
                        //DrawingVisual MyDrawingVisual = new DrawingVisual();


                        // could variate AnkLposX and AnkLPosY
                        if (Math.Abs(AnkRPosX - AnkLPosX) <= Math.Abs(DistSeparateFoot + DistErrorL) && Math.Abs(AnkRPosX - AnkLPosX) >= Math.Abs((DistSeparateFoot) - DistErrorL))
                        {
                            feetOK = true;
                            check = true;
                        }                      

             
            return check;
        }//close method AreFeetseparate

        private void CalculateTracePoints(Skeleton received)
        {
            Joint elbow, wrist,shoulder;
            double longArm, distanceNextPoint;
            int[] grades = {45,50,45};

            if (armExercise == ArmRorL.LEFT)
            {                
                wrist = received.Joints[JointType.WristLeft];
                elbow = received.Joints[JointType.ElbowLeft];
                shoulder = received.Joints[JointType.ShoulderLeft];
            }
            else
            {
                wrist = received.Joints[JointType.WristRight];
                elbow = received.Joints[JointType.ElbowRight];
                shoulder = received.Joints[JointType.ShoulderRight];
            }

            longArm = Math.Sqrt(
                                Math.Pow((elbow.Position.X - wrist.Position.X), 2) +
                                Math.Pow((elbow.Position.Y - wrist.Position.Y), 2) +
                                Math.Pow((elbow.Position.Z - wrist.Position.Z), 2)
                                );
           
            pointsBaseArmExercise[0] = this.SkeletonPointToScreen(wrist.Position);

            pointsBaseArmExercise[3] = this.SkeletonPointToScreen(shoulder.Position);
            pointsBaseArmExercise[3].X = pointsBaseArmExercise[0].X;

            double distanceShoulderWrist = Math.Abs(pointsBaseArmExercise[3].Y - pointsBaseArmExercise[0].Y);

            pointsBaseArmExercise[1].X = pointsBaseArmExercise[0].X;
            pointsBaseArmExercise[1].Y = pointsBaseArmExercise[3].Y + distanceShoulderWrist / 3;

            pointsBaseArmExercise[2].X = pointsBaseArmExercise[0].X;
            pointsBaseArmExercise[2].Y = pointsBaseArmExercise[3].Y + (distanceShoulderWrist / 3) * 2;

            distanceNextPoint = (Math.Sin(grades[0] / 2) * longArm)*2;


        }

        private void DrawTracePoints(DrawingContext drawingContext)
        {
            Brush circleBrush = Brushes.Gray;

            for(int i = 0; i < pointsBaseArmExercise.Length; i++ ){                

                switch (jointsBaseArmExerciseState[i])
                {
                    case StateTracePoints.INITIAL:
                        circleBrush = Brushes.Red;
                    break;
                    case StateTracePoints.ONEPASSED:
                        circleBrush = Brushes.Yellow;
                    break;
                    case StateTracePoints.COMPLETE:
                        circleBrush = Brushes.Green;
                    break;
                }

                drawingContext.DrawEllipse(Brushes.White, null, pointsBaseArmExercise[i], 8, 8);
                drawingContext.DrawEllipse(circleBrush, null, pointsBaseArmExercise[i], 6, 6);
            }
 

        }
    }
}
