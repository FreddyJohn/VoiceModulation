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
<p>if the user grants the application permission then when you select a modulation then it's given conditional inside MainActivity will be met and the moduation parameters will render to the screen by reusing the ModulateControls class</p>
<img width="720" alt="selected_mod_condition" src="https://user-images.githubusercontent.com/39596344/164350340-345aaa07-2064-458b-a05f-66c0230f77b3.png">

<p>Now, in order to hear the selected modulation with your given parameters we override the onClickListener for the play button inside ModulateControls like so</p>
<img width="689" alt="modulate_listener" src="https://user-images.githubusercontent.com/39596344/164351043-127604f1-5b11-4efd-beb5-6e385663ac06.png">

<p>To write this modulation to the Project we override the onLongClickListener for the play button inside ModulateControls like so</p>
<img width="630" alt="modulate_write_listener" src="https://user-images.githubusercontent.com/39596344/164351530-14dcbe46-974e-4830-9bfe-f3c216694ffa.png">
