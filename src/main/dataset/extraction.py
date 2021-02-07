import sys
import pandas as pd
import numpy as np

dev_file = "./features/developer.csv"
pub_file = "./features/publisher.csv"
gen_file = "./features/genre.csv"
prod_file= "./features/game.csv"
basic_url = "http://www.vgchartz.com"

def getDeveloper(df):
    dev = df.Developer.unique()
    #print(type(dev))
    #print(dev)
    return dev

def getPublisher(df):
    pub = df.Publisher.unique()
    #print(type(pub))
    #print(pub)
    return pub

def getGenre(df):
    cat = df.Genre.unique()
    return cat

def createDeveloperFile(dev):
    df = pd.DataFrame(dev)
    l = pd.notnull(df[0])
    df = df[l]
    df.to_csv(dev_file, sep=";", header=False)

def createPublisherFile(pub):
    df = pd.DataFrame(pub)
    l = pd.notnull(df[0])
    df = df[l]
    df.to_csv(pub_file, sep=";", header=False)

def createGenreFile(cat):
    df = pd.DataFrame(cat)
    l = pd.notnull(df[0])
    df = df[l]
    df.to_csv(gen_file, sep=";", header=False)


def defineProduct(df, gen, pub, dev):
    head = ['name','basename','id_genre','year','plateform','ESRB','url_image','id_publisher','id_developer']
    l = []
    for index, row in df.iterrows() :
        p = buildProduct(row, gen, pub, dev)
        l.append(p)
    prod = pd.DataFrame(l)#,columns=head)
    for i in range(9):
        l = pd.notnull(prod[i])
        prod = prod[l]
    prod.to_csv(prod_file, sep=";", header=False)

def buildProduct(row, gen, pub, dev):
    res = []
    res.append(row.Name.replace(';',','))
    res.append(row.basename.replace(';',','))
    res.append(matchGenre(row, gen))
    res.append(row.Year)
    res.append(row.Platform.replace(';',','))
    res.append(row.ESRB_Rating)
    res.append(basic_url + row.img_url)
    res.append(matchPublisher(row, pub))
    if not pd.isna(row.Developer):
        res.append(int(matchDeveloper(row, dev)))
    return res
            
def matchGenre(row, gen):
    l = np.where(gen ==row.Genre)[0]
    return l[0]

def matchPublisher(row, pub):
    l =np.where(row.Publisher == pub)[0]
    return l[0]

def matchDeveloper(row, dev):
    l= np.where(row.Developer == dev)[0]
    return l[0]

def buildImage(row):
    return basic_url + row.img_url
    
if __name__ == "__main__":
    
    print(sys.argv[1])
    
    filename = sys.argv[1]
    df = pd.read_csv(filename)
    dev = getDeveloper(df)   
    createDeveloperFile(dev)
    
    pub = getPublisher(df)
    createPublisherFile(pub)
    
    gen = getGenre(df)
    createGenreFile(gen)
    defineProduct(df, gen, pub, dev)