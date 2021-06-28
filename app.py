from flask import Flask, jsonify, request
import numpy as np
import keras
from keras.models import load_model


app = Flask(__name__)
model = load_model("restmodel.h5")
@app.route('/')
def index():
    return "Hello world!"
@app.route('/get_flags', methods=['GET', 'POST'])
def get_flags():
    data = request.data.decode("utf-8") 
    
    data = data.replace("[", '').replace("]", '').replace("\"", '').split(", ")
    data = [float(x) for x in data]
    data = np.array(data).reshape((1, 10, 50))
    op = model.predict(data)
    op = op[0]
    return jsonify(str(op[0])+" "+ str(op[1])+" "+str(op[2])+" "+str(op[3])+" "+str(op[4])+" "+str(op[5]))

if __name__ == '__main__':
    app.run(host="192.168.1.8", port=5000)