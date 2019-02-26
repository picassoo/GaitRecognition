# -*- coding: utf-8 -*-
from os import listdir
from os.path import isfile, join, isdir
import numpy as np
from sklearn import neighbors,metrics,tree,svm
from scipy.signal import find_peaks_cwt
from sklearn.ensemble import RandomForestClassifier
from statsmodels import robust
from scipy.stats import kurtosis, skew
from sklearn.naive_bayes import GaussianNB
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import MinMaxScaler

ratio = 4

def readDirectoryFile(directory):  
    liste = [f for f in listdir(directory) if isfile(join(directory, f))] 
    return liste

def getMag(row):
     x = row[0]
     y = row[1]
     z = row[2]
     return np.sqrt(x*x + y*y + z*z)

def getPeaksAverage(signal):
    index = find_peaks_cwt(signal, np.arange(1,75))
    return len(index)/len(signal)

def getChangeOfAccuracy(person):
    totalChange = np.empty((len(person)-1,3))
    x = 0.0
    y = 0.0
    z = 0.0
    prevX = person[0][0]
    prevY = person[0][1]
    prevZ = person[0][2]

    for i in range(1,len(person)):
        x += abs(person[i][0]-prevX)
        y += abs(person[i][1]-prevY)
        z += abs(person[i][2]-prevZ)
        prevX = person[i][0]
        prevY = person[i][1]
        prevZ = person[i][2]
        totalChange[i-1] = [x,y,z]

    x /=len(person)
    y /=len(person)
    z /=len(person)

    return x,y,z


def getFourierFT(person):
    singleSignal = np.zeros(len(person))

    index = 0
    for row in person:
        singleSignal[index] = getMag(row)
        index += 1
    
    tff = np.fft.fft(singleSignal)
    return tff

def getList(path,subFiles):
    test = []
    train = []
    allData = []
    i = 0
    for file in subFiles:
        log = []
        fol = open(path+"/"+file,"r") 
        j=0
        print(i)
        i += 1
        for line in fol: 
            rows = line.replace("\n","").split("\t")
            if(j != 0):
                x = float(rows[1])
                y = float(rows[2])
                z = float(rows[3])
                log.append([x,y,z])
            j += 1
        allData.append(log)

    minLimit = min(map(len, allData))
    trainBound = (int)(minLimit/(ratio+1))*ratio

    size = minLimit - trainBound

    label = np.arange(0,len(allData)*ratio)

    for person in allData:
        test.append(person[trainBound:minLimit])

    index = 0
    for person in allData:
        for i in range(1,ratio+1):
            train.append(person[size*(ratio-1):size*ratio])
            label[index*ratio+i-1] = index
        index += 1

    return train,test,label

def getCalculateFeature(person):

    singleSignal = np.zeros(len(person))
    index = 0
    for row in person:
        singleSignal[index] = getMag(row)
        index += 1

    rateAccX,rateAccY,rateAccZ = getChangeOfAccuracy(person)

    peaks_average = getPeaksAverage(singleSignal)
    
    featureList = [np.mean(singleSignal),
                   np.std(singleSignal),
                   np.median(singleSignal),
                   rateAccX, rateAccY, rateAccZ,
                   peaks_average,robust.mad(singleSignal),
                   skew(singleSignal),kurtosis(singleSignal)
                ]

    return featureList

def getData(accSignalData):

    tempPerson = np.zeros((len(accSignalData), 10))
    index = 0
    for row in accSignalData:
        #cov = np.cov((row-rowG))
        #eig = np.linalg.eig(cov)
        tempPerson[index] = getCalculateFeature(row)
        index+=1
        print(index)
    return tempPerson

def arrayMerge(a1,a2):
    mergedArray = np.zeros((len(a1),2*len(a1[0])))    

    index = 0
    for i,j in zip(a1,a2):
        temp = []
        temp.extend(i)
        temp.extend(j)
        mergedArray[index] = temp
        index+=1

    return mergedArray
def printArray(args):
    print("\t".join(args))
def confussionMatrix(labelTest,ynew):
    matrix = np.zeros((21,21))
    for i, j in zip(labelTest,ynew):
      matrix[i, j] += 1
    for row in matrix:
        printArray([str(int(x)) for x in row])
    print("----------------------------")

if __name__ == "__main__":
    
    data = np.load("merged20/gro_3_20.npy")
    label = np.load("merged20/gro_label_3_20.npy")

    print(data.shape)

    train, test, trainLabel, testLabel = train_test_split(data, label, test_size = 0.20, random_state = 42)

    knn = neighbors.KNeighborsClassifier()
    clf = tree.DecisionTreeClassifier()
    gnb = GaussianNB()
    rfc = RandomForestClassifier()
    svmc = svm.SVC()

    trainFeature = getData(train)
    limit = trainFeature.shape[0]

    testFeature = getData(test)

    trainFeature = np.concatenate((trainFeature,testFeature),axis=0)

    scaler = MinMaxScaler(copy=True, feature_range=(0, 1))
    scaler.fit(trainFeature)

    normalizeTrainFeature = scaler.transform(trainFeature)

    normalizeTestFeature = normalizeTrainFeature[limit:]
    normalizeTrainFeature = normalizeTrainFeature[:limit]

    testFeature = trainFeature[limit:]
    trainFeature = trainFeature[:limit]

    print(trainFeature.shape)

    knnTrain = knn.fit(normalizeTrainFeature,trainLabel)
    knnTest  = knn.predict(normalizeTestFeature)

    treeTrain =  clf.fit(trainFeature,trainLabel)
    treeTest = clf.predict(testFeature)

    gnbTrain = gnb.fit(trainFeature,trainLabel)
    gnbTest = gnb.predict(testFeature)

    rfcTrain = rfc.fit(trainFeature,trainLabel)
    rfcTest = rfc.predict(testFeature)

    svmTrain = svmc.fit(normalizeTrainFeature, trainLabel)
    svmTest = svmc.predict(normalizeTestFeature)

    print("KNN Accuracy:", metrics.accuracy_score(testLabel, knnTest))
    print("DECISION TREEE Accuracy:", metrics.accuracy_score(testLabel, treeTest))
    print("GNB Accuracy:", metrics.accuracy_score(testLabel, gnbTest))
    print("RFC Accuracy:", metrics.accuracy_score(testLabel, rfcTest))
    print("SVM Accuracy:", metrics.accuracy_score(testLabel, svmTest))
  
    
    confussionMatrix(testLabel,knnTest)
    confussionMatrix(testLabel,treeTest)
    confussionMatrix(testLabel,gnbTest)
    confussionMatrix(testLabel,rfcTest)
    