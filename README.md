<h1>VoiceModulation</h1>
<p>This is a lightweight digital audio workstation for android created fully from scratch</p>
<h2>Features</h2>
<ol>
  <li>14 time domain variable parameter modulations</li>
  <li>undo/redo</li>
  <li>project save</li>
  <li>edit operations, add/delete</li>
</ol>

<h2>Screenshot of VoiceModuation</h2>
<img width="278" alt="app_example" src="https://user-images.githubusercontent.com/39596344/164349760-768b1181-a61b-403d-92e8-bf84fde1a917.png">

<h3>How are modulations implemented?</h3>

<p><a https://github.com/FreddyJohn/VoiceModulation/blob/master/app/src/main/java/com/adams/voicemodulation/MainActivity.java />if the user grants the application permission then when you select a modulation then it's given conditional inside MainActivity will be met and the moduation parameters will render to the screen by reusing the ModulateControls class</p>
<img width="720" alt="selected_mod_condition" src="https://user-images.githubusercontent.com/39596344/164350340-345aaa07-2064-458b-a05f-66c0230f77b3.png">

<p>Now, in order to hear the selected modulation with your given parameters we override the onClickListener for the play button inside ModulateControls like so</p>
<img width="689" alt="modulate_listener" src="https://user-images.githubusercontent.com/39596344/164351043-127604f1-5b11-4efd-beb5-6e385663ac06.png">

<p>To write this modulation to the Project we override the onLongClickListener for the play button inside ModulateControls like so</p>
<img width="618" alt="modulate_write_listener" src="https://user-images.githubusercontent.com/39596344/164351644-94fd3dfc-7bfe-4f96-958e-d6aa76987a7e.png">

<p>Finally, inside of of the Modulation class there is numerous modulations that implement the effect interface</p>
<img width="393" alt="modulation_interface" src="https://user-images.githubusercontent.com/39596344/164351845-66a82f3e-e023-43cc-8565-92c972b61ff4.png">

<p>Since we selected Phasor, the Phasor modulation we be called inside of the Modulations class and written to a temporary memory space so that the modulation can either be played or written to the Project</p>
<img width="634" alt="phaser_modulation" src="https://user-images.githubusercontent.com/39596344/164352420-9e3a830c-29f2-45f7-abe4-a92569ed29c1.png">

<p>If we did not write it this way then we would need a view for each additional modulation. Writting it this way allows us to take advantage functional programming and treat the modulations as if they were variables and reuse the ModulateControls view as seen here.</p>
<img width="547" alt="functional" src="https://user-images.githubusercontent.com/39596344/164352163-80ac3ec1-2d6f-4264-b882-c952b87490ea.png">

<h2>Features/Improvements</h2>
<ol>
  <li>Many more time domain modulations</li>
  <li>frequency domain operations such as pitch shifting</li>
  <li>After modulations are written to Project the changes should propegate to the waveform held in GraphLogic</li>
  <li>Improvements to core PieceTable data structure</li>
  <li>Better interface for managing projects</li>
  <li>real time effects</li>
  <li>Improvements to GraphLogic Memory</li>
  <li>Multi track projects</li>
  <li>Supporting more file formats</li>
  <li>Anything you would like to add!</li>
</ol>
