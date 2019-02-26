from flask import Flask,request
from keras.models import model_from_json
import numpy as np
from sklearn import neighbors,metrics,tree,svm
from sklearn.ensemble import RandomForestClassifier
from sklearn.naive_bayes import GaussianNB
from sklearn.preprocessing import MinMaxScaler
import json
import time

app = Flask(__name__)
personList = {0: "person0", 1: "person1", 2: "person2", 3: "person3", 4: "person4", 5: "person5",6: "person6",
7: "person7", 8: "person8",9: "person9", 10: "person10", 11: "person11",
12: "person12", 13: "person13", 14: "muhammed",
15: "diyar", 16: "emre", 17: "fatma",
18: "mahmut", 19: "merve", 20: "serhat",21:"Professor"}

def get_models():
    global model_acc_3_20
    global model_acc_4_20
    global model_acc_5_20
    global model_acc_6_20

    global model_gro_3_20
    global model_gro_4_20
    global model_gro_5_20
    global model_gro_6_20

    model_acc_3_20 = compile('model_acc_3_21.json','model_acc_3_21.h5')
    model_acc_4_20 = compile('model_acc_4_21.json','model_acc_4_21.h5')
    model_acc_5_20 = compile('model_acc_5_21.json','model_acc_5_21.h5')
    model_acc_6_20 = compile('model_acc_6_20.json','model_acc_6_20.h5')

    model_gro_3_20 = compile('model_gro_3_21.json','model_gro_3_21.h5')
    model_gro_4_20 = compile('model_gro_4_21.json','model_gro_4_21.h5')
    model_gro_5_20 = compile('model_gro_5_21.json','model_gro_5_21.h5')
    model_gro_6_20 = compile('model_gro_6_21.json','model_gro_6_21.h5')

def compile(json,h5):
    json_file = open(json,'r')
    loaded_model_json = json_file.read()
    json_file.close()
    model = model_from_json(loaded_model_json)
    model.load_weights(h5)
    model._make_predict_function() 
    model.compile(loss='categorical_crossentropy', optimizer='rmsprop', metrics=['accuracy'])

    return model

def getResult(time,sensor,value):
    result = -1
    if(sensor=="acc"):
       if(time==3):
           result = model_acc_3_20.predict_classes(value)[0].item()
       elif(time==4):
           result = model_acc_4_20.predict_classes(value)[0].item()
       elif(time==5):
           result = model_acc_5_20.predict_classes(value)[0].item()
       elif(time==6):
           result = model_acc_6_20.predict_classes(value)[0].item()
    elif(sensor=="gro"):
       if (time == 3):
           result = model_gro_3_20.predict_classes(value)[0].item()
       elif(time == 4):
           result = model_gro_4_20.predict_classes(value)[0].item()
       elif(time == 5):
           result = model_gro_5_20.predict_classes(value)[0].item()
       elif(time == 6):
           result = model_gro_6_20.predict_classes(value)[0].item()
    return result

get_models()
@app.route('/data',methods=['GET','POST'])
def data():
    valueName = request.json["name"]
    valueSensor = request.json["sensor"]
    valueData = request.json["data"]
    value = np.array([valueData])
    ts = time.time()
    person ="collect/"+valueName+"-"+valueSensor+"-"+str(ts)+".npy"
    np.save(person,value)
    return json.dumps({"walk":"added"})

@app.route('/',methods=['GET','POST'])
def index():
    valueSensor = request.json["sensor"]
    valueTime = request.json["time"]
    print("Sensor :: ",valueSensor)
    print("Time :: ",valueTime)
    value = request.json['data']
    value = np.array([value])
    print("------------------")
    print("data shape : \n",value.shape)
    print("------------------")
    person = personList[getResult(valueTime,valueSensor,value)]
    return json.dumps({"walk":person})
 
if __name__ =='__main__':
    app.run(host="0.0.0.0",port=4244,debug=True)
