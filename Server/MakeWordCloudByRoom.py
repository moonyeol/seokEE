from collections import Counter
from konlpy.tag import Okt
import pytagcloud
import pymysql as my

room = "56470"


connection = None
row = None # 로그인 결과를 담는 변수
try:
    connection = my.connect(host='seokee0503.c9p1xpsot2og.ap-northeast-2.rds.amazonaws.com', # DB 주소
                            user='indexoutofrange',      # DB 접속 계정
                            password='12341234', # DB 접속 비번
                            db='seokee',   # Database 이름
                            #port=3306,        # Port     
                            charset='utf8')
                            # cursorclass=my.cursors.DictCursor) # Cursor Type
    if connection:
        print('DB OPEN')
        #####################################################
        with connection.cursor() as cursor:
            sql    = '''
                select msg from talk where room="%s";
            '''  % (room)
            cursor.execute( sql )
            row    = cursor.fetchall()  # 하나의 row를 뽑을때
        #####################################################
except Exception as e:
    print('->', e)
    row = None
finally:
    if connection:
        connection.close()
        print('DB Close')
x = ""
for i in range(len(row)):
    x=x+" "+row[i][0]
nlp = Okt()
nouns = nlp.nouns(x)
count = Counter(nouns)
tag2 = count.most_common(40)
print(tag2)
taglist = pytagcloud.make_tags(tag2, maxsize=80)
pytagcloud.create_tag_image(taglist, room+'.jpg', size=(700, 500), fontname='Korean', rectangular=False)
