   M S B A M L   `   `   `  ����  SkeletonBasics-WPF8    2Microsoft.Samples.Kinect.SkeletonBasics.MainWindow   5   	   \ XPresentationFramework, Version=4.0.0.0, Culture=neutral, PublicKeyToken=31bf3856ad364e35R NWindowsBase, Version=4.0.0.0, Culture=neutral, PublicKeyToken=31bf3856ad364e35W SPresentationCore, Version=4.0.0.0, Culture=neutral, PublicKeyToken=31bf3856ad364e35D 9http://schemas.microsoft.com/winfx/2006/xaml/presentation    5   	   R NSystem.Xaml, Version=4.0.0.0, Culture=neutral, PublicKeyToken=b77a5c561934e089:x,http://schemas.microsoft.com/winfx/2006/xaml     5   	   -   5          � Title$  Skeleton Basics��5   	   $	��735��6!   $��1038.656��6.    � 	Resources 5         MediumGreyBrush �� Color  KinectPurpleBrush  KinectBlueBrush �� 
TargetType �� Property �� Value  SquareCheckBox �� 
TargetType �� Orientation �Name	 ��Name
 ��Name �� TextAlignment r�Name r� Source �� Triggers P� Property P� Value �� 
TargetName��%�  &	        &	 #     &	 F     '�� i     &	 �     7  7  �� 5   
   $ 	#ff6e6e6e��62   �� 5   
   $ 	#ff52318f��64   �� 5	   
   $ 	#ff00BCF2��62   �� 5
   
   " ��6   .�5      ��  �@J�SnapsToDevicePixels . 64   5      �� 5   
   " ��6   .�5      ��  � �� 5      �� 5      " ��6&   .R�5      � .+�5      �� $ 
Horizontal=�6)   
������ .+�5   "   � $ SquareCheckBoxChecked��6'   .+�5   &   �� $	 CheckedNormal��6,   $��Images\CheckedNormal.png��6C   $
��None=�6e   $��Center=�6t   �� 5   &   $	 CheckedHover��6,   $��Images\CheckedHover.png��6B   $
��None=�6c   $��Center=�6r   $y�	Collapsed=�6�   5   #   � 5   "   $ SquareCheckBoxUnchecked��6'   $y�	Collapsed=�6H   .+�5   &   �� $	 UncheckedNormal��6,   $ ��Images\UncheckedNormal.png��6E   $
��None=�6i   $��Center=�6x   �� 5   &   $	 UncheckedHover��6,   $��Images\UncheckedHover.png��6D   $
��None=�6g   $��Center=�6v   $y�	Collapsed=�6�   5   #   �� 5   "   $
 SquareCheckBoxText��6,   #��z��6H   $
 Left=�6i   $��Center=�6   8��  $��15(�6�   $��9,0,0,0q�6�   5      r� 5      $ backgroundMusic��6+   $ Music\background.mp3?�6E   $��0��6c   $��0��6n   5       5      P� 5        �@d�	IsChecked$ falseY�6;   . �5!   "   �� $ SquareCheckBoxChecked��6Q    � y�$ 	Collapsed=�6?   6Q   �� 5"   "   $ SquareCheckBoxUnchecked��6O    � y�$ Visible=�6?   6O   5#      P� 5$       �@J�IsMouseOver . . �5%   "   �� $ CheckedNormal��6Q    � y�$ 	Collapsed=�6?   6Q   �� 5&   "   $ UncheckedNormal��6Q    � y�$ 	Collapsed=�6?   6Q   �� 5'   "   $ CheckedHover��6O    � y�$ Visible=�6?   6O   �� 5(   "   $ UncheckedHover��6O    � y�$ Visible=�6?   6O   �� 5)   "   8  $ SquareCheckBoxText��6h    � ��5*      5,      5.      5/      50      .��52      � -   5        $��
layoutGrid��52      $��	10 0 10 0q�6    � RowDefinitions 53   
   �� 54      $
��Auto �6   �� 55      $��* �6   �� 56      $
��Auto �6   �� 57      $
��Auto �6   �� 58      $
��Auto �6   .+�5:   
   ]� $��0��6   $��0 0 0 20q�6!   .+�5;      �� $
��Left=�6   $��Images\Logo.png��6*   $
��Fill=�6C   $��32��6R   $��81��6_   $��0 10 0 5q�6j   �� 5<      $��Right=�6   $��0 0 -1 0q�6/   $��Bottom=�6A   #��[  6\   $��Segoe UI)�6�   $��18(�6�   Fitness Exercises6�   �� 5=      $��1��6   $��Images\Status.png��6$   $
��None=�6?   $��Center=�6N   $��0 0 0 5q�6k   5>      (� 5?   
   $��2��6    (� Stretch$ Uniform=�6   $��Center=�61   5A      � 5B   
   -   5        $
��Grid��5B      }�. .+�5C      �� -   5        $��
ColorImage��5C      $	��593��6&   $	��480��62   �� 5D      -   5        $��Image��5D      $	��593��6!   $	��480��6-   �� 5E      -   5         ��Name$ canvas��5E      .+�5F      �� -   5        $
��mano��5F      $��Images\Mano.png��6$   $y�Visible=�6=   $��30��6R   $��Center=�6]    �� Left$	 300��6z   
 �� Top$	 300��6�   �� 5G      -   5        $��Medal��5G      $��Images\Gold.png��6%   $y�Hidden=�6>   $	��100��6R   $��Center=�6^   $	 699��6{   $ 65��6�   �� 5H      -	   5        $��poseEstandar��5H      $��Images\poseEstandar.png��6,   $y�Hidden=�6M   $
��Auto��6a   $	��140��6o   $��Center=�6{   $	 424��6�   $	 110��6�   �� 5I      -
   5        $��
poseArriba��5I      $��Images\poseArriba.png��6*   $y�Hidden=�6I   $
��Auto��6]   $	��140��6k   $��Center=�6w   $	 424��6�   $	 180��6�   �� 5J      -   5        $��poseArribaAmbos��5J      $ ��Images\poseArribaAmbos.png��6/   $y�Hidden=�6T   $
��Auto��6h   $	��140��6v   $��Center=�6�   $	 424��6�   $	 180��6�   �� 5K      -   5        $��poseEstandarAmbos��5K      $"��Images\poseEstandarAmbos.png��61   $y�Hidden=�6W   $
��Auto��6k   $	��140��6y   $��Center=�6�   $	 424��6�   $	 180��6�   �� 5L      -   5        $��	mancuerna��5L      $��Images\mancuerna.png��6)   $y�Hidden=�6G   $ 10��6[   $	 150��6l   $��60��6}   $��Center=�6�   �� 5M      -   5        $��
mancuerna2��5M      $��Images\mancuerna.png��6*   $y�Hidden=�6H   $ 10��6\   $	 150��6m   $��60��6~   $��Center=�6�   5N      5O      �� 5P   
   -   5        $��checkBoxSeatedMode��5P   �   $��2��6   #��[ 6    $��Seated Mode��6H   $
��Auto��6^   $
��Left=�6l   $��Center=�6�   $��
0 10 10 10q�6�   6�   �� 5Q   
   -   5        $��	statusBar��5Q   ?   $��3��6   $��Stretch=�6!   $��Bottom=�6P   
�������#��[  6~   .@�5R      �� $��0 0 0 10q�6   .��5S      �� -   5        $��statusBarText��5S      $��-1 0 0 0q�61   20Click 'Seated' to change skeletal pipeline type!6u   5T      5U      �� 5V   
   -   5        $��
textBoxLog��5V   T   $��4��6   $��36��6   $��Stretch=�6+   $��0q�6I   $��Bottom=�6f   $��texto��6�   5W      5X      