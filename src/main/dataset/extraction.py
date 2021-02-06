import sys
import pandas as pd
import numpy as np

dev_file = "developer.csv"
pub_file = "publisher.csv"
gen_file = "genre.csv"
prod_file= "product.csv"
basic_url = "http://www.vgchartz.com"

def getDeveloper(df):
    dev = df.Developer.unique()
    print(type(dev))
    print(dev)
    return dev

def getPublisher(df):
    pub = df.Publisher.unique()
    print(type(pub))
    print(pub)
    return pub

def getGenre(df):
    cat = df.Genre.unique()
    return cat

def createDeveloperFile(dev):
    df = pd.DataFrame(dev)
    df['description']= ""
    df.to_csv(dev_file, header=['name','description'])

def createPublisherFile(pub):
    df = pd.DataFrame(pub)
    df['description']= ""
    df.to_csv(pub_file , header=['name','description'])

def createGenreFile(cat):
    df = pd.DataFrame(cat)
    df.to_csv(gen_file, header=['name'])


def defineProduct(df, gen, pub, dev):
    head = ['name','basename','id_genre','year','plateform','ESRB','url_image','id_publisher','id_developer']
    l = []
    for index, row in df.iterrows() :
        p = buildProduct(row, gen, pub, dev)
        l.append(p)
    prod = pd.DataFrame(l,columns=head)
    prod.to_csv(prod_file)

def buildProduct(row, gen, pub, dev):
    res = []
    res.append(row.Name)
    res.append(row.basename)
    res.append(matchGenre(row, gen))
    res.append(row.Year)
    res.append(row.Platform)
    res.append(row.ESRB_Rating)
    res.append(basic_url + row.img_url)
    res.append(matchPublisher(row, pub))
    if not pd.isna(row.Developer):
        res.append(matchDeveloper(row, dev))
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
    #createDeveloperFile(dev)
    
    pub = getPublisher(df)
    #createPublisherFile(pub)
    
    gen = getGenre(df)
    #createGenreFile(gen)
    defineProduct(df, gen, pub, dev)