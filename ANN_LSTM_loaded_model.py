import numpy as np
from keras.models import model_from_json
from keras.utils import np_utils
from numpy import int8
from sklearn.model_selection import train_test_split

def printArray(args):
    print("\t".join(args))

if __name__ == "__main__":

    # load json and create model
    json_file = open('model_gro_3_21.json', 'r')
    loaded_model_json = json_file.read()
    json_file.close()
    loaded_model = model_from_json(loaded_model_json)
    # load weights into new model
    loaded_model.load_weights("model_gro_3_21.h5")
    print("Loaded model from disk")

    #dataTest = np.load("LSTM_10_test.npy")
    #labelTest = np.load("LSTM_10_test_label.npy")

    #labelTest = np_utils.to_categorical(labelTest-1)

    # evaluate loaded model on test data
    loaded_model.compile(loss='categorical_crossentropy', optimizer='rmsprop', metrics=['accuracy'])
    #score = loaded_model.evaluate(dataTest,labelTest,batch_size=7)
    #print("%s: %.2f%%" % (loaded_model.metrics_names[1], score[1] * 100))
    me = np.load("merged21/gro_3_21.npy")
    meLabel = np.load("merged21/gro_label_3_21.npy")
    print(me.shape)
    matrix = np.zeros((22,22))
    
    dataTrain, dataTest, labelTrain, labelTest = train_test_split(me, meLabel, test_size = 0.20, random_state = 42)
    
    ynew = loaded_model.predict_classes(dataTest)
    
    for i , j in zip(ynew,labelTest):
        print( i,"--",j)
    
    score = 0
    
    for i, j in zip(labelTest,ynew):
        matrix[i, j] += 1
        if i == j:
            score += 1
    print("Score :"+str(score/len(ynew)))
    
    for row in matrix:
        printArray([str(int(x)) for x in row])