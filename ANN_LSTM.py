# -*- coding: utf-8 -*-

import numpy as np

from keras.models import Sequential
from keras.layers import Dense,BatchNormalization
from keras.layers import LSTM,Activation
from keras.layers.convolutional import Conv1D
from keras.layers.convolutional import MaxPooling1D
from keras.utils import np_utils
from sklearn.model_selection import train_test_split

def printArray(args):
    print("\t".join(args))

if __name__ == "__main__":
    
    data = np.load("merged21/gro_5_21.npy")
    label = np.load("merged21/gro_label_5_21.npy")
    

    dataTrain, dataTest, labelTrain, labelTest = train_test_split(data, label, test_size = 0.20, random_state = 42)


    print(dataTrain.shape)
    print(dataTest.shape)
    print(labelTrain.shape)
    print(labelTest.shape)

    n_classes = 22

    labelTrain = np_utils.to_categorical(labelTrain)
    labelTest = np_utils.to_categorical(labelTest)

    print("Type ::" ,type(labelTest))

    model = Sequential()
    epochs = 40
    kernel_size = 1 #kernel_size of 1 worked surprisingly well
    pool_size = 2
    dropout_rate = 0.15
    f_act = 'sigmoid'

    batch_size = 7
    model = Sequential()
    model.add(Conv1D(512, (kernel_size), input_shape=(None,3), activation=f_act, padding='same'))
    model.add(BatchNormalization())
    model.add(MaxPooling1D(pool_size=(pool_size)))
    #model.add(Dropout(dropout_rate))
    model.add(Conv1D(64, (kernel_size), activation=f_act, padding='same'))
    model.add(BatchNormalization())
    model.add(MaxPooling1D(pool_size=(pool_size)))
    #model.add(Dropout(dropout_rate))
    model.add(Conv1D(32, (kernel_size), activation=f_act, padding='same'))
    model.add(BatchNormalization())
    model.add(LSTM(128, return_sequences=True))
    model.add(LSTM(128, return_sequences=True))
    model.add(LSTM(128, return_sequences=True))
    model.add(LSTM(128, return_sequences=True))
    model.add(LSTM(128))
    #model.add(Dropout(0.4))
    model.add(Dense(n_classes))
    model.add(BatchNormalization())
    model.add(Activation('softmax'))
    
    model.compile(loss='categorical_crossentropy',
                  optimizer='rmsprop',
                  metrics=['accuracy'])
    model.summary()



    model.fit(dataTrain, labelTrain,batch_size=batch_size,validation_data=None,epochs=epochs,shuffle=True)

    score = model.evaluate(dataTest,labelTest, batch_size=batch_size)
    print(score)
    
    model_json = model.to_json()
    with open("model_gro_5_21.json", "w") as json_file:
        json_file.write(model_json)
    # serialize weights to HDF5
    model.save_weights("model_gro_5_21.h5")
    print("Saved model to disk")
    predictLabel = model.predict_classes(dataTest)
    print(predictLabel)
    print(type(predictLabel))
    print(predictLabel.shape)
    print(type(labelTest))
    matrix = np.zeros((n_classes, n_classes),dtype=int)
   
   