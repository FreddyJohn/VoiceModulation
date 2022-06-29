<h1>VoiceModulation</h1>
<p>This is a lightweight digital audio workstation for android created fully from scratch, open source, and available for free on the Google Play Store.</p>
<h2>Features</h2>
<ol>
  <li>14 time domain variable parameter modulations</li>
  <li>dynamic set operations: find/add/delete</li>
  <li>project save</li>
  <li>undo/redo</li>
</ol>

<h2>Screenshot of VoiceModulation</h2>
<img width="278" alt="app_example" src="https://user-images.githubusercontent.com/39596344/164349760-768b1181-a61b-403d-92e8-bf84fde1a917.png">

<h3>How can I contribute/How are modulations implemented?</h3>
<p><a href="https://github.com/FreddyJohn/VoiceModulation/blob/master/app/src/main/java/com/adams/voicemodulation/MainActivity.java"/>if the user grants the application permission then when you select a modulation it's given conditional inside MainActivity will be met and the moduation parameters will render to the screen by reusing the ModulateControls class</a></p>
<img width="720" alt="selected_mod_condition" src="https://user-images.githubusercontent.com/39596344/164350340-345aaa07-2064-458b-a05f-66c0230f77b3.png">

<p><a href="https://github.com/FreddyJohn/VoiceModulation/blob/master/app/src/main/java/com/adams/voicemodulation/controls/ModulateControls.java"/>Now, in order to hear the selected modulation with your given parameters we override the onClickListener for the play button inside ModulateControls like so</a></p>
<img width="689" alt="modulate_listener" src="https://user-images.githubusercontent.com/39596344/164351043-127604f1-5b11-4efd-beb5-6e385663ac06.png">

<p><a href="https://github.com/FreddyJohn/VoiceModulation/blob/master/app/src/main/java/com/adams/voicemodulation/controls/ModulateControls.java"/>To write this modulation to the Project we override the onLongClickListener for the play button inside ModulateControls like so</a></p>
<img width="618" alt="modulate_write_listener" src="https://user-images.githubusercontent.com/39596344/164351644-94fd3dfc-7bfe-4f96-958e-d6aa76987a7e.png">

<p><a href="https://github.com/FreddyJohn/VoiceModulation/blob/master/app/src/main/java/com/adams/voicemodulation/signal/Modulation.java"/>Finally, inside of of the Modulation class there is numerous modulations that implement the effect interface and override it's modulate method</a></p>
<img width="393" alt="modulation_interface" src="https://user-images.githubusercontent.com/39596344/164351845-66a82f3e-e023-43cc-8565-92c972b61ff4.png">

<p><a href="https://github.com/FreddyJohn/VoiceModulation/blob/master/app/src/main/java/com/adams/voicemodulation/signal/Modulation.java"/>Since we selected Phaser, the Phaser modulation will be called inside of the Modulations class and written to a temporary memory space so that the modulation can either be played or written to the Project</a></p>
<img width="634" alt="phaser_modulation" src="https://user-images.githubusercontent.com/39596344/164352420-9e3a830c-29f2-45f7-abe4-a92569ed29c1.png">

<h4>Why are the Modulations implemented this way?</h4>
<p>If we did not write it this way then we would need a new view for each additional modulation. Writting it this way allows us to take advantage of functional programming and treat the modulations as if they were variables, reusing the UI code for modulations via the ModulateControls class. This makes it extremely simple for anyone to add a new effect. All you must do is create a new modulation by implementing the effect interface and overriding the modulate method inside of the Modulation class. Then create a new conditional inside of MainAcitivity where you pass your new modulation and its arbitrary number parameters to ModulateControls where each parameter can be adjusted by the user</p>

<h2>Future Features & Improvements</h2>
<ol>
  <li>Many more time domain modulations</li>
  <li>frequency domain operations such as pitch shifting</li>
  <li>After modulations are written to Project the changes should propagate to the waveform held in GraphLogic</li>
  <li>Improvements to core PieceTable data structure</li>
  <li>Better interface for managing projects</li>
  <li>real time effects</li>
  <li>Improvements to GraphLogic Memory</li>
  <li>Multi track projects</li>
  <li>Supporting more file formats</li>
  <li>writing it again in swift</li>
  <li>Anything you would like to add!</li>
</ol>
