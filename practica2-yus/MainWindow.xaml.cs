//------------------------------------------------------------------------------
// <copyright file="MainWindow.xaml.cs" company="Microsoft">
//     Copyright (c) Microsoft Corporation.  All rights reserved.
// </copyright>
//------------------------------------------------------------------------------

namespace Microsoft.Samples.Kinect.SkeletonBasics
{
    using System.Globalization;    
    using System.IO;
    using System.Windows;
    using System;        
    using System.Windows.Media;
    using System.Windows.Media.Imaging;    
    using Microsoft.Kinect;
    using System.Windows.Controls;

    /// <summary>
    /// Interaction logic for MainWindow.xaml
    /// </summary>
    public partial class MainWindow : Window
    {

        /// <summary>
        /// Bitmap that will hold color information
        /// </summary>
        private WriteableBitmap colorBitmap;

        /// <summary>
        /// Intermediate storage for the color data received from the camera
        /// </summary>
        private byte[] colorPixels;

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
            INITIAL, LETMOVE, MEDALTIME, DONEMOV, FINISHED
        };

        public enum ArmRorL
        {
            RIGHT,LEFT, BOTH, NONE
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
        Point[] pointsBaseArmExercise2 = new Point[4];

        public enum StateTracePoints
        {
            INITIAL, ONEPASSED, COMPLETE
        };


        StateTracePoints[] pointsBaseArmExerciseState = { StateTracePoints.INITIAL, 
                                                          StateTracePoints.INITIAL, 
                                                          StateTracePoints.INITIAL, 
                                                          StateTracePoints.INITIAL 
                                                        };

        StateTracePoints[] pointsBaseArmExerciseState2 = { StateTracePoints.INITIAL, 
                                                          StateTracePoints.INITIAL, 
                                                          StateTracePoints.INITIAL, 
                                                          StateTracePoints.INITIAL 
                                                        };

        /// <summary>
        /// StateMov used for know actual state mov
        /// </summary> 
        private StateMov stateMov = StateMov.INITIAL;

        private double bigCircleRadius = 8;

        private double littleCircleRadius = 6;

        private int puntuation = 0;

        private int totalPuntuation = 0;

        private int totalSeries = 2;

        private int totalRepeats = 3;

        private int actualCircleSerie = 0;

        private int actualRepeat = 0;

        private int actualSerie = 0;

        TimeSpan medalTimeStart, medalTimeStop;

        double medalTime = 3000;

        private const int puntuationPositive = 10;

        public enum DirectionMov
        {
            UP,DOWN
        };

        DirectionMov dirMov = DirectionMov.UP;

        private int totalExercises = 3;

        private int actualExercise = 0;

        public enum CircleExercise
        {
            INPROCESS, WELLDONE, WRONG
        };

        public enum MedalType
        {
            GOLD, SILVER, BRONZE, NONE
        };

        private class ScoreByExercise
        {
            public MedalType medal;
            public int puntuation;
            TimeSpan timeStart;
            TimeSpan timeFinish;

            public ScoreByExercise()
            {
                this.medal = MedalType.NONE;
                this.puntuation = 0;
                this.timeStart = new TimeSpan(DateTime.Now.Ticks);
                this.timeFinish = timeStart;
            }

            public void SetStart(){
                this.timeStart = new TimeSpan(DateTime.Now.Ticks);
            }

            public void SetFinish()
            {
                this.timeFinish = new TimeSpan(DateTime.Now.Ticks);
            }

            public TimeSpan GetStart()
            {
                return this.timeStart;
            }

            public TimeSpan GetFinish()
            {
                return this.timeFinish;
            }
        }

        ScoreByExercise exercise1Score = new ScoreByExercise();
        ScoreByExercise exercise2Score = new ScoreByExercise();
        ScoreByExercise exercise3Score = new ScoreByExercise();

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
            //backgroundMusic.Play();
          /*  MediaElement background = new MediaElement();
            background.Source = (new Uri(@"Music\background.mp3", UriKind.RelativeOrAbsolute));
            background.Play();*/

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
                // Turn on the color stream to receive color frames
                this.sensor.ColorStream.Enable(ColorImageFormat.RgbResolution640x480Fps30);

                // Allocate space to put the pixels we'll receive
                this.colorPixels = new byte[this.sensor.ColorStream.FramePixelDataLength];

                // This is the bitmap we'll display on-screen
                this.colorBitmap = new WriteableBitmap(this.sensor.ColorStream.FrameWidth, this.sensor.ColorStream.FrameHeight, 96.0, 96.0, PixelFormats.Bgr32, null);

                // Set the image we display to point to the bitmap where we'll put the image data
                this.ColorImage.Source = this.colorBitmap;

                // Add an event handler to be called whenever there is new color frame data
                this.sensor.ColorFrameReady += this.SensorColorFrameReady;

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
        /// Event handler for Kinect sensor's ColorFrameReady event
        /// </summary>
        /// <param name="sender">object sending the event</param>
        /// <param name="e">event arguments</param>
        private void SensorColorFrameReady(object sender, ColorImageFrameReadyEventArgs e)
        {
            using (ColorImageFrame colorFrame = e.OpenColorImageFrame())
            {
                if (colorFrame != null)
                {
                    // Copy the pixel data from the image to a temporary array
                    colorFrame.CopyPixelDataTo(this.colorPixels);

                    // Write the pixel data into our bitmap
                    this.colorBitmap.WritePixels(
                        new Int32Rect(0, 0, this.colorBitmap.PixelWidth, this.colorBitmap.PixelHeight),
                        this.colorPixels,
                        this.colorBitmap.PixelWidth * sizeof(int),
                        0);
                }
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
                dc.DrawRectangle(Brushes.Transparent, null, new Rect(0.0, 0.0, RenderWidth, RenderHeight));

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

                DrawGUI(dc);                
            }
        }

        private void DrawGUI(DrawingContext dc)
        {
            Brush textColor = Brushes.White;
            int textSize = 18,exercisePrint=0,seriePrint=0,repeatPrint=0;          
            Point pText = new Point(10, 15);
            Point psText;
            FormattedText text = text = new FormattedText("NoText", CultureInfo.CurrentCulture, FlowDirection, new Typeface("Verdana"), textSize, textColor),
                          scoreText,
                          infoExerciseText;;

            dc.DrawRectangle(Brushes.DarkOrange, new Pen(Brushes.White, 3), new Rect(0, 0, RenderWidth, 50));

            switch (stateMov)
            {
                case StateMov.INITIAL:
                    text = new FormattedText("Colócate en la posición inicial del ejercicio.", CultureInfo.CurrentCulture, FlowDirection, new Typeface("Verdana"), textSize, textColor);                                        
                    break;
                case StateMov.LETMOVE:
                    text = new FormattedText("Realiza los movimientos pasando por los círculos ", CultureInfo.CurrentCulture, FlowDirection, new Typeface("Verdana"), textSize, textColor);                    
                   
                    this.DrawTracePoints(dc);

                    if (actualExercise == 2)
                    {
                        this.DrawTracePoints2(dc);
                    }
                    break;
                case StateMov.MEDALTIME:
                    switch (CheckMedalWon())
                    {
                        case MedalType.GOLD:
                            text = new FormattedText("¡Perfecto! Has conseguido una medalla de Oro.", CultureInfo.CurrentCulture, FlowDirection, new Typeface("Verdana"), textSize, textColor);
                            Medal.Source = new BitmapImage(new Uri(@"Images/gold.png", UriKind.RelativeOrAbsolute));
                            break;
                        case MedalType.SILVER:
                            text = new FormattedText("¡Muy bien! Has conseguido una medalla de Plata.", CultureInfo.CurrentCulture, FlowDirection, new Typeface("Verdana"), textSize, textColor);
                            Medal.Source = new BitmapImage(new Uri(@"Images/silver.png", UriKind.RelativeOrAbsolute));
                            break;
                        case MedalType.BRONZE:
                            text = new FormattedText("¡Bien! Has conseguido una medalla de Bronce.", CultureInfo.CurrentCulture, FlowDirection, new Typeface("Verdana"), textSize, textColor);
                            Medal.Source = new BitmapImage(new Uri(@"Images/bronze.png", UriKind.RelativeOrAbsolute));
                            break;
                        default:
                            text = new FormattedText("¡Espantoso! No consigues ni la medalla de chocolate.", CultureInfo.CurrentCulture, FlowDirection, new Typeface("Verdana"), textSize, textColor);
                            Medal.Source = new BitmapImage(new Uri(@"Images/nothing.png", UriKind.RelativeOrAbsolute));
                            break;
                    }                                   
                    break;
                case StateMov.DONEMOV:
                    text = new FormattedText("Ejercicio completado ¡Bien hecho!", CultureInfo.CurrentCulture, FlowDirection, new Typeface("Verdana"), textSize, textColor);                                       
                    break;
                case StateMov.FINISHED:
                    text = new FormattedText("¡Has completado todos los ejercicios!", CultureInfo.CurrentCulture, FlowDirection, new Typeface("Verdana"), textSize, textColor);

                    break;
            }

            pText.X = RenderWidth/2 - text.Width / 2;
            dc.DrawText(text, pText);

            if (stateMov != StateMov.INITIAL && stateMov != StateMov.FINISHED)
            {
                scoreText = new FormattedText("Puntuación: " + puntuation, CultureInfo.CurrentCulture, FlowDirection, new Typeface("Verdana"), 25, textColor);
                psText = new Point((RenderWidth / 2) - scoreText.Width / 2, 100);
                dc.DrawText(scoreText, psText);
            }

            FormattedText scoreByExerciseText = new FormattedText(" Puntuación total: " + totalPuntuation +
                                                                  "\n Ejercicio 1: " + exercise1Score.GetFinish().Subtract(exercise1Score.GetStart()).TotalSeconds.ToString("0.00") + "s " + exercise1Score.medal +
                                                                  "\n Ejercicio 2: " + exercise2Score.GetFinish().Subtract(exercise2Score.GetStart()).TotalSeconds.ToString("0.00") + "s " + exercise2Score.medal +
                                                                  "\n Ejercicio 3: " + exercise3Score.GetFinish().Subtract(exercise3Score.GetStart()).TotalSeconds.ToString("0.00") + "s " + exercise3Score.medal,
                                                                   CultureInfo.CurrentCulture, FlowDirection, new Typeface("Verdana"), 16, textColor);
            Point sbeText = new Point((RenderWidth - scoreByExerciseText.Width - 10), RenderHeight - scoreByExerciseText.Height - 10);

            if (stateMov == StateMov.MEDALTIME || stateMov == StateMov.DONEMOV)
            {
                exercisePrint = actualExercise + 1;
                seriePrint = actualSerie + 1;
                repeatPrint = actualRepeat; 
            }
            else if (stateMov == StateMov.FINISHED)
            {
                exercisePrint = actualExercise;
                seriePrint = actualSerie;
                repeatPrint = actualRepeat;
            }
            else
            {
                exercisePrint = actualExercise + 1;
                seriePrint = actualSerie + 1;
                repeatPrint = actualRepeat + 1; 
            }

            infoExerciseText = new FormattedText(" Ejercicio " + exercisePrint + "/" + totalExercises +
                                                               "     Serie " + seriePrint + "/" + totalSeries +
                                                               "     Repetición " + repeatPrint + "/" + totalRepeats,
                                                                CultureInfo.CurrentCulture, FlowDirection, new Typeface("Verdana"), 16, textColor);

            Point ieText = new Point(10, 65);

            if (stateMov != StateMov.FINISHED)
            {
                dc.DrawRectangle(Brushes.SlateBlue, new Pen(Brushes.White, 2), new Rect((RenderWidth - scoreByExerciseText.Width - 20), RenderHeight - scoreByExerciseText.Height - 20, scoreByExerciseText.Width + 20, scoreByExerciseText.Height + 20));
                dc.DrawText(scoreByExerciseText, sbeText);
                dc.DrawRectangle(Brushes.SlateBlue, new Pen(Brushes.White, 2), new Rect(0, 60, 390, 30));
                dc.DrawText(infoExerciseText, ieText);
            }
            else
            {
                int heightFinish = 70;
                MedalType finalMedal = MedalType.NONE;

                if ((totalPuntuation / (totalSeries * totalExercises)) > 180)
                {
                    finalMedal = MedalType.GOLD;
                    Medal.Source = new BitmapImage(new Uri(@"Images/gold.png", UriKind.RelativeOrAbsolute));
                }
                else if ((totalPuntuation / (totalSeries * totalExercises)) > 150)
                {
                    finalMedal = MedalType.SILVER;
                    Medal.Source = new BitmapImage(new Uri(@"Images/silver.png", UriKind.RelativeOrAbsolute));
                }
                else if ((totalPuntuation / (totalSeries * totalExercises)) > 100)
                {
                    finalMedal = MedalType.BRONZE;
                    Medal.Source = new BitmapImage(new Uri(@"Images/bronze.png", UriKind.RelativeOrAbsolute));
                }
                else
                {
                    Medal.Source = new BitmapImage(new Uri(@"Images/nothing.png", UriKind.RelativeOrAbsolute));
                }

                Medal.Visibility = Visibility.Visible;

                Point pfText = new Point((RenderWidth / 2) - 150, heightFinish+10);
                FormattedText finishText = new FormattedText(" Puntuación Final: " + totalPuntuation +
                                                                  "\n\n Ejercicio 1:"+
                                                                     "\n   - Tiempo tardado: "+exercise1Score.GetFinish().Subtract(exercise1Score.GetStart()).TotalSeconds.ToString("0.00") + " segundos " +
                                                                     "\n   - Medalla parcial: "+ exercise1Score.medal +
                                                                     "\n   - Puntuación parcial: " + exercise1Score.puntuation + " puntos" +
                                                                  "\n\n Ejercicio 2:" +
                                                                     "\n   - Tiempo tardado: " + exercise2Score.GetFinish().Subtract(exercise2Score.GetStart()).TotalSeconds.ToString("0.00") + " segundos " +
                                                                     "\n   - Medalla parcial: " + exercise2Score.medal +
                                                                     "\n   - Puntuación parcial: " + exercise2Score.puntuation + " puntos"+
                                                                  "\n\n Ejercicio 3:" +
                                                                     "\n   - Tiempo tardado: " + exercise3Score.GetFinish().Subtract(exercise3Score.GetStart()).TotalSeconds.ToString("0.00") + " segundos " +
                                                                     "\n   - Medalla parcial: " + exercise3Score.medal +
                                                                     "\n   - Puntuación parcial: " + exercise3Score.puntuation + " puntos" +
                                                                     "\n\n\n MEDALLA FINAL: "+finalMedal,
                                                                   CultureInfo.CurrentCulture, FlowDirection, new Typeface("Verdana"), 16, textColor);

                dc.DrawRectangle(Brushes.SlateBlue, new Pen(Brushes.White, 2), new Rect((RenderWidth / 2) - (finishText.Width+20)/2, heightFinish, finishText.Width + 20, finishText.Height+20));
                dc.DrawText(finishText, pfText);
            }
        }

        private double CalculateHeightUser(Skeleton received){
            double height = 0;

            SkeletonPoint Head = received.Joints[JointType.Head].Position;
            SkeletonPoint Ankle = received.Joints[JointType.AnkleRight].Position;

            height = (SkeletonPointToScreen(Head).Y - SkeletonPointToScreen(Ankle).Y);

            return Math.Abs(height);

        }

        private double CalculatePosXUser(Skeleton received)
        {
            double posX = 0;

            SkeletonPoint shoulder = received.Joints[JointType.ShoulderLeft].Position;

            posX = SkeletonPointToScreen(shoulder).X;

            return Math.Abs(posX);

        }

        private void DumbbellPosition(Skeleton received)
        {
            double handX = 0, handY=0, hand2X = 0, hand2Y = 0;
            int yplus = 0;

            switch (armExercise)
            {
                case ArmRorL.LEFT:
                    handX = SkeletonPointToScreen(received.Joints[JointType.HandLeft].Position).X;
                    handY = SkeletonPointToScreen(received.Joints[JointType.HandLeft].Position).Y;
                    Canvas.SetLeft(mancuerna,handX+155);
                    Canvas.SetTop(mancuerna, handY+yplus);  
                break;
                case ArmRorL.RIGHT:
                    handX = SkeletonPointToScreen(received.Joints[JointType.HandRight].Position).X;
                    handY = SkeletonPointToScreen(received.Joints[JointType.HandRight].Position).Y;
                    Canvas.SetLeft(mancuerna,handX+150);
                    Canvas.SetTop(mancuerna, handY+yplus);  
                break;
                case ArmRorL.BOTH:
                    handX = SkeletonPointToScreen(received.Joints[JointType.HandRight].Position).X;
                    handY = SkeletonPointToScreen(received.Joints[JointType.HandRight].Position).Y;
                    hand2X = SkeletonPointToScreen(received.Joints[JointType.HandLeft].Position).X;
                    hand2Y = SkeletonPointToScreen(received.Joints[JointType.HandLeft].Position).Y;
                    Canvas.SetLeft(mancuerna,handX+150);
                    Canvas.SetTop(mancuerna, handY+yplus); 
                    Canvas.SetLeft(mancuerna2,hand2X+155);
                    Canvas.SetTop(mancuerna2, hand2Y+yplus);                    
                break;
            }                      
        }       

        private void ImagesManagement(Skeleton received)
        {
            DumbbellPosition(received);

            // poseEstandar.Height = CalculateHeightUser(skeleton);            
            //poseEstandar.Margin = new Thickness(CalculatePosXUser(skeleton), 6, 629, -1);
            poseEstandar.Opacity = 0.50;
            poseEstandarAmbos.Opacity = 0.50;

            double shoulderLeftX = SkeletonPointToScreen(received.Joints[JointType.ShoulderLeft].Position).X;
            double headY = SkeletonPointToScreen(received.Joints[JointType.Head].Position).Y;

            if (stateMov == StateMov.INITIAL)
            {
                if (actualExercise == 2)
                {
                    Canvas.SetLeft(poseEstandarAmbos, shoulderLeftX);
                    Canvas.SetTop(poseEstandarAmbos, headY);
                }
                else if (actualExercise == 1)
                {
                    Canvas.SetLeft(poseEstandar, shoulderLeftX);
                    Canvas.SetTop(poseEstandar, headY);
                }
                else
                {
                    Canvas.SetLeft(poseEstandar, shoulderLeftX );
                    Canvas.SetTop(poseEstandar, headY);
                }
            }
            else if (stateMov == StateMov.LETMOVE)
            {
                if (actualExercise == 2)
                {
                    Canvas.SetLeft(poseArribaAmbos, shoulderLeftX);
                    Canvas.SetTop(poseArribaAmbos, headY);
                }
                else if (actualExercise == 1)
                {
                    Canvas.SetLeft(poseArriba, shoulderLeftX);
                    Canvas.SetTop(poseArriba, headY);
                }
                else
                {
                    Canvas.SetLeft(poseArriba, shoulderLeftX);
                    Canvas.SetTop(poseArriba, headY);
                }
            }
            

            if (actualExercise == 1)
            {
                poseArriba.RenderTransformOrigin = new Point(0.5, 0.5);
                ScaleTransform flipTrans = new ScaleTransform();
                flipTrans.ScaleX = -1;
                poseArriba.RenderTransform = flipTrans;
            }

            if (actualRepeat == 0 && stateMov == StateMov.LETMOVE)
            {
                if (pointsBaseArmExerciseState[0] == StateTracePoints.INITIAL)
                {
                    if (actualExercise == 2 && actualSerie == 0)
                    {
                        poseArribaAmbos.Visibility = Visibility.Visible;
                        poseArribaAmbos.Opacity = 0.60;
                    }
                    else if(actualSerie == 0)
                    {
                        poseArriba.Visibility = Visibility.Visible;
                        poseArriba.Opacity = 0.60;

                    }
                }
                else if (pointsBaseArmExerciseState[1] == StateTracePoints.INITIAL)
                {
                    if (actualExercise == 2)
                    {
                        poseArribaAmbos.Opacity = 0.40;
                    }
                    else
                    {
                        poseArriba.Opacity = 0.40;
                    }
                }
                else if (pointsBaseArmExerciseState[2] == StateTracePoints.INITIAL)
                {
                    if (actualExercise == 2)
                    {
                        poseArribaAmbos.Opacity = 0.30;
                    }
                    else
                    {
                        poseArriba.Opacity = 0.30;
                    }
                }
                else if (pointsBaseArmExerciseState[3] != StateTracePoints.INITIAL)
                {
                    if (actualExercise == 2)
                    {
                        poseArribaAmbos.Opacity = 0;
                        poseArribaAmbos.Visibility = Visibility.Hidden;
                    }
                    else
                    {
                        poseArriba.Opacity = 0;
                        poseArriba.Visibility = Visibility.Hidden;
                    }
                }
            }

        }

        private void ProgramMechanical(Skeleton skeleton)
        {
            ImagesManagement(skeleton);            

            if (MyFitnessExercise(skeleton, distanceMov27))
            {
                switch (stateMov)
                {
                    case StateMov.INITIAL:
                        stateMov = StateMov.LETMOVE;
                        poseEstandar.Visibility = Visibility.Hidden;
                        poseEstandarAmbos.Visibility = Visibility.Hidden;
                        mancuerna.Visibility = Visibility.Visible;
                        
                        if (actualExercise == 2)
                        {
                            mancuerna2.Visibility = Visibility.Visible;
                        }
                        if (actualSerie == 0)
                        {
                            switch (actualExercise)
                            {
                                case 0:
                                    exercise1Score.SetStart();
                                    exercise1Score.SetFinish();
                                    break;
                                case 1:
                                    exercise2Score.SetStart();
                                    exercise2Score.SetFinish();
                                    break;
                                case 2:
                                    exercise3Score.SetStart();
                                    exercise3Score.SetFinish();
                                    break;
                            }
                        }
                        break;
                    case StateMov.LETMOVE:                        

                        if (actualSerie == (totalSeries-1))
                        {
                            stateMov = StateMov.MEDALTIME;
                            Medal.Visibility = Visibility.Visible;
                            medalTimeStart = new TimeSpan(DateTime.Now.Ticks);

                            mancuerna.Visibility = Visibility.Hidden;

                            if (actualExercise == 2)
                            {
                                mancuerna2.Visibility = Visibility.Hidden;
                            }

                            switch (actualExercise)
                            {
                                case 0:
                                    exercise1Score.SetFinish();
                                    break;
                                case 1:
                                    exercise2Score.SetFinish();
                                    break;
                                case 2:
                                    exercise3Score.SetFinish();
                                    break;
                            }      

                            actualSerie = 0;
                        }
                        else
                        {                            
                            actualSerie++;
                        }

                        RestartExerciseValues();

                        break;
                    case StateMov.MEDALTIME:
                        medalTimeStop = new TimeSpan(DateTime.Now.Ticks);
                        if (medalTimeStop.Subtract(medalTimeStart).TotalMilliseconds > medalTime)
                        {
                            Medal.Visibility = Visibility.Hidden;
                            stateMov = StateMov.DONEMOV;
                        }
                        break;
                    case StateMov.DONEMOV:
                        actualExercise++;

                        if (actualExercise == 1)
                        {
                            poseEstandar.RenderTransformOrigin = new Point(0.5, 0.5);
                            ScaleTransform flipTrans = new ScaleTransform();
                            flipTrans.ScaleX = -1;
                            poseEstandar.RenderTransform = flipTrans;
                            poseEstandar.Visibility = Visibility.Visible;
                        }
                        else if(actualExercise == 2)
                        {
                            poseEstandarAmbos.Visibility = Visibility.Visible;
                        }

                        if (actualExercise != totalExercises)
                        {
                            nextExercise();
                            stateMov = StateMov.INITIAL;
                        }
                        else
                        {
                            stateMov = StateMov.FINISHED;
                        }
                        break;
                }
            }           
        }

        private void RestartExerciseValues()
        {            
            actualCircleSerie = 0;
            actualRepeat = 0;
            actualCircleSerie = 0;
            dirMov = DirectionMov.UP;

            for (int i = 0; i < pointsBaseArmExercise.Length; i++)
                pointsBaseArmExerciseState[i] = StateTracePoints.INITIAL;                      
        }
        
        private void nextExercise(){
             puntuation = 0;

            if (actualExercise == 1)
            {
                armExercise = ArmRorL.LEFT;
                armRepose = ArmRorL.RIGHT;
                armReposeOK = false;
                armExerciseOK = false;
                bodyOK = false;
                feetOK = false;
            }
            else if (actualExercise == 2)
            {
                armExercise = ArmRorL.BOTH;
                armRepose = ArmRorL.NONE;
            }
        }

        /// <summary>
        /// Draws a skeleton's bones and joints
        /// </summary>
        /// <param name="skeleton">skeleton to draw</param>
        /// <param name="drawingContext">drawing context to draw to</param>
        private void DrawBonesAndJoints(Skeleton skeleton, DrawingContext drawingContext)
        {
            ProgramMechanical(skeleton);   
      
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
        /// Return if my fitness exercise goes OK - José Francisco Bravo Sánchez
        /// </summary>
        /// <param name="skeleton">skeleton to check</param>
        /// <param name="distance">input data. Distance to move the foot</param>        
        private bool MyFitnessExercise(Skeleton skeleton, float distance)
        {            
            bool check = false;            
            Joint ankleLeft = skeleton.Joints[JointType.AnkleLeft],
                  ankleRight = skeleton.Joints[JointType.AnkleRight],
                  kneeLeft = skeleton.Joints[JointType.KneeLeft],
                  kneeRight = skeleton.Joints[JointType.KneeRight],
                  hipCenter = skeleton.Joints[JointType.HipCenter];                  

            switch (stateMov)
            {
                case StateMov.INITIAL:

                    if (armExercise != ArmRorL.BOTH)
                    {      
                        IsAlignedBody(skeleton);
                        AreArmStraight(skeleton);
                        AreArm90grades(skeleton);
                        AreFeetSeparate(skeleton);

                        if (feetOK && bodyOK && armExerciseOK && armReposeOK)
                        {
                            check = true;
                            CalculateTracePoints(skeleton);
                        }
                    }
                    else
                    {
                        armExercise = ArmRorL.LEFT; //first check if left arm is straight

                        IsAlignedBody(skeleton);
                        AreArmStraight(skeleton);                        
                        AreFeetSeparate(skeleton);

                        if ((bodyOK && armExerciseOK && feetOK))
                        {
                            armReposeOK = true;
                            armExercise = ArmRorL.RIGHT; //second check if right arm is straight
                            AreArmStraight(skeleton);

                            if (armExerciseOK)
                            {
                                check = true;
                                CalculateTracePoints(skeleton);

                                for (int i = 0; i < pointsBaseArmExercise.Length; i++)
                                {
                                    pointsBaseArmExercise2[i] = pointsBaseArmExercise[i];
                                }

                                armExercise = ArmRorL.LEFT;

                                CalculateTracePoints(skeleton);
                            }
                            else
                            {
                                armReposeOK = false;
                            }
                        }

                        armExercise = ArmRorL.BOTH; //Restaure actual exercises arm
                    }
                    break;
                case StateMov.LETMOVE:
                    CircleExercise stateMovCircle;

                    if (actualExercise == 2)
                    {
                        stateMovCircle = CheckMovSimultaneous(skeleton);
                    }
                    else
                    {
                        stateMovCircle = CheckMov(skeleton);
                    }

                    ChangePuntuation(stateMovCircle);

                    if (actualRepeat == totalRepeats)
                    {
                        ChangePuntuation(CircleExercise.WELLDONE);
                        check = true;                        

                    }
                    break;
                case StateMov.MEDALTIME:
                    check = true;
                    break;
                case StateMov.DONEMOV:
                    check = true;         
                    break;
            }

            textBoxLog.Clear();
            textBoxLog.Text = "ArmRepose: "+armReposeOK+" ArmExercise: "+armExerciseOK+" Body: "+bodyOK+" Feet:"+feetOK;

            return check;
        }

        
    // boolean method that return true if body is completely aligned
        private void IsAlignedBody(Skeleton received)
        {
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
                bodyOK = true;           
            }

        }

        private void AreArm90grades(Skeleton received)
        {
            armReposeOK = false;
            double shoulderX, elbowX, elbowY, elbowZ, wristY,wristZ;
            float distErrorSE = 0.15f;
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
                armReposeOK = true;
            } 
        }

        private void AreArmStraight(Skeleton received)
        {           
            armExerciseOK = false;
            //caldulate admited error 5% that correspond to 9 degrees for each side
            double radian = (9 * Math.PI) / 180;
            double wristX,wristY, projectedWristX, distError,hipY;

            switch (armExercise)
            {

                case ArmRorL.RIGHT:
                    wristX = received.Joints[JointType.WristRight].Position.X;
                    wristY = received.Joints[JointType.WristRight].Position.Y;
                   // double WriRPosZ = received.Joints[JointType.WristRight].Position.Z;
                    
                    double ShouRPosX = received.Joints[JointType.ShoulderRight].Position.X;
                    double ShouRPosY = received.Joints[JointType.ShoulderRight].Position.Y;
                    //double ShouRPosZ = received.Joints[JointType.ShoulderRight].Position.Z; 

                    hipY = received.Joints[JointType.HipRight].Position.Y;

                    projectedWristX = ShouRPosX;

                    double distShouLtoWristR = ShouRPosY - wristY;           
                    distError = distShouLtoWristR * Math.Tan(radian);

                break;
                default: //case LEFT
                    wristX = received.Joints[JointType.WristLeft].Position.X;                      
                    wristY = received.Joints[JointType.WristLeft].Position.Y;
                   // double WriLPosZ = received.Joints[JointType.WristLeft].Position.Z;

                    double ShouLPosX = received.Joints[JointType.ShoulderLeft].Position.X;
                    double ShouLPosY = received.Joints[JointType.ShoulderLeft].Position.Y;
                   // double ShouLPosZ = received.Joints[JointType.ShoulderLeft].Position.Z;

                    hipY = received.Joints[JointType.HipLeft].Position.Y;

                    projectedWristX = ShouLPosX;

                    double distShouLtoWristL = ShouLPosY - wristY;
                    distError = distShouLtoWristL * Math.Tan(radian);
                break;
            }

            if ((Math.Abs(wristX - projectedWristX) <= distError) && wristY < hipY)
            {         
                armExerciseOK = true;
            }
        }
      
        //method for the second position feet separate between (gradesAnkleHipCenter*2) degrees to be accepted
        private void AreFeetSeparate(Skeleton received)
        {
            double gradesAnkleHipCenter = 20;
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
            }                      
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

                switch (pointsBaseArmExerciseState[i])
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

                drawingContext.DrawEllipse(Brushes.White, null, pointsBaseArmExercise[i], bigCircleRadius, bigCircleRadius);
                drawingContext.DrawEllipse(circleBrush, null, pointsBaseArmExercise[i], littleCircleRadius, littleCircleRadius);
            }           

        }

        private void DrawTracePoints2(DrawingContext drawingContext)
        {
            Brush circleBrush = Brushes.Gray;

            for (int i = 0; i < pointsBaseArmExercise2.Length; i++)
            {

                switch (pointsBaseArmExerciseState2[i])
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

                drawingContext.DrawEllipse(Brushes.White, null, pointsBaseArmExercise2[i], bigCircleRadius, bigCircleRadius);
                drawingContext.DrawEllipse(circleBrush, null, pointsBaseArmExercise2[i], littleCircleRadius, littleCircleRadius);
            }
        }

        private CircleExercise CheckMov(Skeleton received)
        {
            CircleExercise actualMov = CircleExercise.INPROCESS;

            Joint hand;
            Point actualHandPoint;

            double AdmittedError = 4;

            if (armExercise == ArmRorL.LEFT)
            {
                hand = received.Joints[JointType.HandLeft];                
            }
            else
            {
                hand = received.Joints[JointType.HandRight];                
            }

            actualHandPoint = this.SkeletonPointToScreen(hand.Position);

            if (Math.Abs(actualHandPoint.Y - pointsBaseArmExercise[actualCircleSerie].Y) < (bigCircleRadius + AdmittedError) &&
                Math.Abs(actualHandPoint.X - pointsBaseArmExercise[actualCircleSerie].X) < (bigCircleRadius + AdmittedError))
            {
                actualMov = CircleExercise.WELLDONE;
                NextPointState();
                NextCircleMov();
            }
            else
            {
                int nextCircleSerie;

                if (dirMov == DirectionMov.DOWN)
                {
                    if (actualCircleSerie == 0)
                    {
                        nextCircleSerie = 0;
                    }
                    else
                    {
                        nextCircleSerie = actualCircleSerie - 1;
                    }

                    if ((actualHandPoint.Y > (pointsBaseArmExercise[actualCircleSerie].Y + bigCircleRadius + AdmittedError)) ||
                        (actualHandPoint.X < (pointsBaseArmExercise[actualCircleSerie].X - bigCircleRadius - AdmittedError)) ||
                        (actualHandPoint.X > (pointsBaseArmExercise[actualCircleSerie].X + bigCircleRadius + AdmittedError)))
                    {
                        actualMov = CircleExercise.WRONG;
                    }
                }
                else
                {
                    if (actualCircleSerie == 3)
                    {
                        nextCircleSerie = 3;
                    }
                    else
                    {
                        nextCircleSerie = actualCircleSerie + 1;
                    }

                    if ((actualHandPoint.Y < (pointsBaseArmExercise[actualCircleSerie].Y + bigCircleRadius + AdmittedError)) ||
                        (actualHandPoint.X < (pointsBaseArmExercise[actualCircleSerie].X - bigCircleRadius - AdmittedError)) ||
                        (actualHandPoint.X > (pointsBaseArmExercise[actualCircleSerie].X + bigCircleRadius + AdmittedError))
                        )
                    {
                        actualMov = CircleExercise.WRONG;
                    }
                }

            }

            return actualMov;
        }

        private void NextPointState()
        {
            switch (pointsBaseArmExerciseState[actualCircleSerie])
            {
                case StateTracePoints.INITIAL:
                    pointsBaseArmExerciseState[actualCircleSerie] = StateTracePoints.ONEPASSED;

                    if (actualExercise == 2)
                    {
                        pointsBaseArmExerciseState2[actualCircleSerie] = StateTracePoints.ONEPASSED;
                    }
                break;
                case StateTracePoints.ONEPASSED:
                    pointsBaseArmExerciseState[actualCircleSerie] = StateTracePoints.COMPLETE;

                    if (actualExercise == 2)
                    {
                        pointsBaseArmExerciseState2[actualCircleSerie] = StateTracePoints.COMPLETE;
                    }
                break;
                case StateTracePoints.COMPLETE:
                    pointsBaseArmExerciseState[actualCircleSerie] = StateTracePoints.INITIAL;

                    if (actualExercise == 2)
                    {
                        pointsBaseArmExerciseState2[actualCircleSerie] = StateTracePoints.INITIAL;
                    }
                break;
            }
        }

        private void NextCircleMov()
        {
            switch (actualCircleSerie)
            {
                case 0:
                    actualCircleSerie = 2;
                    if(dirMov == DirectionMov.DOWN){
                        actualRepeat++;
                        for (int i = 0; i < pointsBaseArmExerciseState.Length; i++)
                        {
                            pointsBaseArmExerciseState[i] = StateTracePoints.INITIAL;

                            if (actualExercise == 2)
                            {
                                pointsBaseArmExerciseState2[i] = StateTracePoints.INITIAL;
                            }
                        }

                        pointsBaseArmExerciseState[0] = StateTracePoints.ONEPASSED;
                        if (actualExercise == 2)
                        {
                            pointsBaseArmExerciseState2[0] = StateTracePoints.ONEPASSED;
                        }
                        dirMov = DirectionMov.UP;                        
                    }
                break;
                case 1:
                    if (dirMov == DirectionMov.DOWN)
                    {
                        actualCircleSerie = 2;
                    }
                    else
                    {
                        actualCircleSerie = 3;
                    }
                break;
                case 2:
                    if (dirMov == DirectionMov.DOWN)
                    {
                        actualCircleSerie = 0;
                    }
                    else
                    {
                        actualCircleSerie = 1;
                    }
                break;
                case 3:
                    actualCircleSerie = 1;
                    pointsBaseArmExerciseState[3] = StateTracePoints.COMPLETE;
                    
                    if (actualExercise == 2)
                    {
                        pointsBaseArmExerciseState2[3] = StateTracePoints.COMPLETE;
                    }

                    if(dirMov == DirectionMov.UP){                        
                        dirMov = DirectionMov.DOWN;                        
                    }
                break;
            }
        }

        private void ChangePuntuation(CircleExercise ce)
        {
            switch (ce)
            {
                case CircleExercise.INPROCESS:
                break;
                case CircleExercise.WELLDONE:
                    puntuation += puntuationPositive;
                break;
                case CircleExercise.WRONG:
                    if (puntuation > 0)
                    {
                        puntuation--;
                    }
                break;
            }
        }

        private MedalType CheckMedalWon()
        {
            MedalType medalWon = MedalType.NONE;

            if (puntuation/totalSeries > 180)
            {
                medalWon = MedalType.GOLD;
            }
            else if (puntuation / totalSeries > 150)
            {
                medalWon = MedalType.SILVER;
            }
            else if (puntuation / totalSeries > 100)
            {
                medalWon = MedalType.BRONZE;
            }

            ActualizeExerciseScore(medalWon);

            return medalWon;
        }

        private void ActualizeExerciseScore(MedalType medalWon)
        {
            switch (actualExercise)
            {
                case 0:
                    if (exercise1Score.puntuation == 0)
                    {
                        totalPuntuation += puntuation;
                        exercise1Score.puntuation = puntuation;
                        exercise1Score.medal = medalWon;
                    }                    
                    break;
                case 1:
                    if (exercise2Score.puntuation == 0)
                    {
                        totalPuntuation += puntuation;
                        exercise2Score.puntuation = puntuation;
                        exercise2Score.medal = medalWon;
                    }
                    break;
                case 2:
                    if (exercise3Score.puntuation == 0)
                    {
                        totalPuntuation += puntuation;
                        exercise3Score.puntuation = puntuation;
                        exercise3Score.medal = medalWon;
                    }
                    break;
            }

            
        }

        private CircleExercise CheckMovSimultaneous(Skeleton received)
        {
            CircleExercise actualMov = CircleExercise.INPROCESS;

            Joint handLeft, handRight;
            Point actualHandPointLeft, actualHandPointRight;

            double AdmittedError = 4;
           
            handLeft = received.Joints[JointType.HandLeft];
            handRight = received.Joints[JointType.HandRight];            

            actualHandPointLeft = this.SkeletonPointToScreen(handLeft.Position);
            actualHandPointRight = this.SkeletonPointToScreen(handRight.Position);

            if (Math.Abs(actualHandPointLeft.Y - pointsBaseArmExercise[actualCircleSerie].Y) < (bigCircleRadius + AdmittedError) &&
                Math.Abs(actualHandPointLeft.X - pointsBaseArmExercise[actualCircleSerie].X) < (bigCircleRadius + AdmittedError) &&
                Math.Abs(actualHandPointRight.Y - pointsBaseArmExercise2[actualCircleSerie].Y) < (bigCircleRadius + AdmittedError) &&
                Math.Abs(actualHandPointRight.X - pointsBaseArmExercise2[actualCircleSerie].X) < (bigCircleRadius + AdmittedError))
            {
                actualMov = CircleExercise.WELLDONE;
                NextPointState();
                NextCircleMov();
            }
            else
            {
                int nextCircleSerie;

                if (dirMov == DirectionMov.DOWN)
                {
                    if (actualCircleSerie == 0)
                    {
                        nextCircleSerie = 0;
                    }
                    else
                    {
                        nextCircleSerie = actualCircleSerie - 1;
                    }

                    if ((actualHandPointLeft.Y > (pointsBaseArmExercise[actualCircleSerie].Y + bigCircleRadius + AdmittedError)) ||
                        (actualHandPointLeft.X < (pointsBaseArmExercise[actualCircleSerie].X - bigCircleRadius - AdmittedError)) ||
                        (actualHandPointLeft.X > (pointsBaseArmExercise[actualCircleSerie].X + bigCircleRadius + AdmittedError)) ||
                        (actualHandPointRight.Y > (pointsBaseArmExercise2[actualCircleSerie].Y + bigCircleRadius + AdmittedError)) ||
                        (actualHandPointRight.X < (pointsBaseArmExercise2[actualCircleSerie].X - bigCircleRadius - AdmittedError)) ||
                        (actualHandPointRight.X > (pointsBaseArmExercise2[actualCircleSerie].X + bigCircleRadius + AdmittedError)))
                    {
                        actualMov = CircleExercise.WRONG;
                    }
                }
                else
                {
                    if (actualCircleSerie == 3)
                    {
                        nextCircleSerie = 3;
                    }
                    else
                    {
                        nextCircleSerie = actualCircleSerie + 1;
                    }

                    if ((actualHandPointLeft.Y < (pointsBaseArmExercise[actualCircleSerie].Y + bigCircleRadius + AdmittedError)) ||
                        (actualHandPointLeft.X < (pointsBaseArmExercise[actualCircleSerie].X - bigCircleRadius - AdmittedError)) ||
                        (actualHandPointLeft.X > (pointsBaseArmExercise[actualCircleSerie].X + bigCircleRadius + AdmittedError)) ||
                        (actualHandPointRight.Y < (pointsBaseArmExercise2[actualCircleSerie].Y + bigCircleRadius + AdmittedError)) ||
                        (actualHandPointRight.X < (pointsBaseArmExercise2[actualCircleSerie].X - bigCircleRadius - AdmittedError)) ||
                        (actualHandPointRight.X > (pointsBaseArmExercise2[actualCircleSerie].X + bigCircleRadius + AdmittedError))
                        )
                    {
                        actualMov = CircleExercise.WRONG;
                    }
                }

            }

            return actualMov;
        }
     
       
    }
}
