#-------------FLASK CODE-------------------
from skimage import io, color
import numpy as np
from flask import Flask,  request, jsonify, render_template

app = Flask(__name__)

base_L = 55.04577059
base_a = 1.651798824
base_b = -30.40452353

@app.route('/')
def home():
    return render_template('index.html')


@app.route('/calibrate', methods=['POST'])
def calibrate():
    if(request.method=="POST"):
        imagefile = request.files['photo']
        img = io.imread(imagefile) # Reading image
        lab = color.rgb2lab(img)   # Converting RGB to LAB
        L = lab[:,:,0] #separating lab components to L, a, b
        a = lab[:,:,1]
        b = lab[:,:,2]
        l_avg = np.mean(L) # Average of LAB image's L pixel's value
        a_avg = np.mean(a) #Average of LAB image's a pixel's value
        b_avg = np.mean(b) #Average of LAB image's b pixel's value

        diff_L = l_avg - base_L
        diff_a = a_avg - base_a
        diff_b = b_avg - base_b

        diff_json = {
            'd_L':diff_L, 'd_a':diff_a, 'd_b':diff_b
        }
    # print(diff_L)
        
    return jsonify(diff_json)


@app.route('/upload', methods=['POST']) # Routing for flask server 
def upload():
    if(request.method=="POST"):
        imagefile = request.files['image']  # Getting selected image from request sent by mobile application 
        radio_button_value = request.form.get('radioButtonValue')
        diff_L = float(request.form['d_L'])
        diff_a = float(request.form['d_a'])
        diff_b = float(request.form['d_b'])
        # print(diff_L)
        first_word = radio_button_value.split()[0]
        img = io.imread(imagefile) # Reading image
        lab = color.rgb2lab(img)   # Converting RGB to LAB
        L = lab[:,:,0] #separating lab components to L, a, b
        a = lab[:,:,1]
        b = lab[:,:,2]
        l_avg = np.mean(L) # Average of LAB image's L pixel's value
        a_avg = np.mean(a) #Average of LAB image's a pixel's value
        b_avg = np.mean(b) #Average of LAB image's b pixel's value
        # print(l_avg)

        #Lens correction
         # Correct the LAB values using the calibration values
        L_corrected = l_avg - diff_L
        a_corrected = a_avg - diff_a
        b_corrected = b_avg - diff_b
        
        # print(L_corrected)

        #Bias correction
        L_bias = L_corrected-37.81
        a_bias = a_corrected-3.49
        b_bias= b_corrected-6.31

        # print(L_bias)

        if(first_word=="Medium"): 
            organic_c = 10.44-0.1998*L_bias   # If soiltype is medium then oraganic_c's value = 10.44-0.1998*lightness
            nitrogen = 0.9080-0.0159*L_bias  # If soiltype is medium then nitrogen = 0.9080-0.0159*lightness
            clay = 49.9230-1.023*L_bias+1.6584*a_bias   # If soiltype is medium then clay = 49.9230-1.023*lightness+1.6584*a

        elif(first_word=="Coarse"):
            organic_c= 9.16-0.163*L_bias    # If soiltype is Coarse then oraganic_c's value = 9.16 – 0.163* Lightness
            nitrogen = 0.9571-0.0171*L_bias   # If soiltype is Coarse then nitrogen = 0.9571-0.0171*lightness  
            clay = 13.1499+1.9314*a_bias-0.9949*b_bias    # If soiltype is Coarse then clay = 13.1499+1.9314*a-0.9949*b

        elif(first_word=="Fine"):
            organic_c = 7.241 - 0.1342 *L_bias  # If soiltype is Fine then oraganic_c's value = 7.241 – 0.1342 * Lightness
            nitrogen = 0.7413-0.0357*b_bias     # If soiltype is Fine then nitrogen value = 0.7413-0.0357*b
            clay = 0.0
        else:
            print('Error')
            
        val_json = {
            'message_org_c':organic_c, #Generating JSON for sending organic carbon output to mobile.
            'message_nit':nitrogen,    #Generating JSON for sending nitrogen output to mobile.
            'message_clay':clay        #Generating JSON for sending clay output to mobile.
        }
        return jsonify(val_json) #Sending organic carbon value to mobile.        
    
    

if __name__ == "__main__":                  # Flask server code
    app.run(debug=True, port=5000)
    # app.run()