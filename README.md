# DEFEND - Android Application of Trust and Safety

<p align="center">
<img src="https://github.com/pranav1152/DEFEND/blob/main/Images/DEFEND.jpg" height="400">
</p>

## TAGLINE
DEFEND is an extension to the existing chat applications, that focuses on detecting fakeness in messages, categories to which it belongs and restricts sensitive and fake content from spreading within the community without compromising the privacy of the user.

## THE PROBLEM IT SOLVES
<ul>
<li>DEFEND uses machine learning to classify the news as fake or real.</li>
<li>Messages are given as inputs to the first layer of the lightweight machine learning model present in the local machine (mobile phone) and the resultant output is sent over to a remote server (that houses the rest of the model). </li>
<li>After the server receives the encodings from the first layer, the model checks if the message is fake or not and decodes the category. </li>
<li>If the message is fake, it raises a flag and alerts both the sender and recipient and restricts them from sending it further. </li>
<li>Privacy is maintained with End-to-End Encryption. </li>
</ul>

## SYSTEM ARCHITECTURE
<p align="center">
<img src="https://github.com/pranav1152/DEFEND/blob/main/Images/System%20Architecture.jpeg" height="400">
</p>

## WORK FLOW
<p align="center">
<img src="https://github.com/pranav1152/DEFEND/blob/main/Images/Workflow.jpeg" height="400">
</p>

## CHALLENGES WE RAN INTO
<ul>
<li>To come up with an efficient yet lightweight model for detecting the fakeness of the message. </li>
<li>Realtime chatting with low latency.</li>
</ul>

## SCREENSHOT
<p align="center">
<img src="https://github.com/pranav1152/DEFEND/blob/main/Images/Output1.jpeg" height="400">
</p>
