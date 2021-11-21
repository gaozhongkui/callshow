# -*- coding:utf-8 -*-
import re
import sys
import json
import time
import js2py
import random
import requests
from tqdm import tqdm
from lxml import etree
from pyquery import PyQuery as pq

"""
目标网址：Pornhub单视频下载
链接来源：APP分享链接或web地址
目标地址：api-> https://pornhub.com/view_video.php?viewkey=ph5f51501da477d
加密原理：
    - 访问目标api：获取网页源代码
    - xpath提取加密js文件：.//div[@id="player"]/script[1]/text()
    - 将获取到的js文件进行切片,仅只需要切片的前一节：resource.split("playerObjList")[0]
    - 每一个视频的真实地址都是被切分成很多随机变量名块，然后将这些变量块组合成完整的视频地址
        - eg：
            var asda= "03/348554751/1080P"; var shdas="phncdn.com/videos/202009/" var sdias="https://dv."
            quality_1080p = sdias+shdas+asda
                          = "https://dv.phncdn.com/videos/202009/03/348554751/1080P" 
    - js2py执行切片好的js文件，构建成一个标准函数：返回flashvars_视频id的值（flashvars_348554751为对象格式）
    
    + 2021-02-22 Pornhub新版加密原理：
        - js2py执行的后的flashvars_348554751返回结果中的mediaDefinitions为一个数组
        - 数组中的尾数就是其真实的视频请求地址链接LinkApi
        - 再一次请求这个LinkApi，返回结果包含各种视频格式及其播放地址
"""


class PornHub(object):
    def __init__(self, url):
        self.url = url
        self.session = requests.Session()
        self.headers = {
            "user-agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) "
                          "Chrome/85.0.4183.102 Safari/537.36"
        }

    def get_keys(self, category, start_page, end_page):
        """
        Query based on incoming parameters
        :param category: video category(e.g., ht:最热门, mv:最多次观看...)
        :param start_page: the start page
        :param end_page: the end page
        :return: json file
        """
        base_url = "https://cn.pornhub.com/video"
        for page in tqdm(range(start_page, end_page + 1), ncols=80):
            params = {
                "o": category,
                "page": page
            }
            try:
                response = self.session.get(url=base_url, params=params, headers=self.headers, timeout=45)
                if response.status_code == 200:
                    doc = pq(response.text)
                    rows = doc("ul#videoCategory li.pcVideoListItem .wrap")
                    # to save lists
                    box_lists = []
                    for row in rows.items():
                        tag_a = row(".phimage a")
                        link_url = "https://pornhub.com" + str(tag_a.attr("href"))
                        title = tag_a("img").attr("alt")
                        cover = tag_a("img").attr("data-src")
                        media_book = tag_a("img").attr("data-mediabook")
                        duration = tag_a(".marker-overlays .duration").text()
                        quality = tag_a(".marker-overlays .hd-thumbnail").text()
                        tag_msg = row(".thumbnail-info-wrapper")
                        author = tag_msg(".usernameWrap a").text()
                        link_author = "https://pornhub.com" + str(tag_msg(".usernameWrap a").attr("href"))
                        views = row(".videoDetailsBlock .views var").text()
                        likes = row(".videoDetailsBlock .rating-container .value").text()
                        info = {
                            "title": title,
                            "link_url": link_url,
                            "cover": cover,
                            "media": {
                                "media_book": media_book,
                                "duration": duration,
                                "quality": quality,
                                "author": author,
                                "link_author": link_author
                            },
                            "views": {
                                "views": views,
                                "likes": likes
                            }
                        }
                        # push to list
                        box_lists.append(info)
                    # save to json file
                    with open("./pornhubs/pornhub-{}-{}.json".format(category, page), "w", encoding="utf-8") as f:
                        f.write(json.dumps(box_lists, ensure_ascii=False))
                    time.sleep(random.randint(2, 5))
            except Exception as e:
                print(e)

    def get_video(self):
        """
        Way to parse encrypt signal video url
        use proxies or you can directly surfer outer-net
        :return: video-object
        """
        res = self.session.get(url=self.url, headers=self.headers, timeout=40)
        if res.status_code == 200:
            try:
                html = etree.HTML(res.text)
                doc = html.xpath('.//div[@id="player"]/script[1]/text()')[0]
                doc = str(doc.split("playerObjList")[0]).strip()
                # find the object of property
                flash_vars = re.findall('flashvars_\d+', doc)[0]
                message = js2py.eval_js("".join(doc) + flash_vars).to_dict()
                # default to choose the best quality
                cover = message["image_url"]
                title = message["video_title"]
                quality = []
                if message["mediaDefinitions"]:
                    video_url = message["mediaDefinitions"][-1]["videoUrl"]
                    result = self.session.get(url=video_url, headers=self.headers, timeout=40)
                    quality = json.loads(result.text)
                else:
                    quality.append('parse url error')
                info = {
                    "title": title,
                    "cover": cover,
                    "quality": quality
                }
                return json.dumps(info, ensure_ascii=False)
            except Exception as e:
                return json.dumps({"info": "暂无相关数据，请检查相关数据：" + str(e)}, ensure_ascii=False)
        else:
            return json.dumps({"info": "暂无相关数据，请检查相关数据："}, ensure_ascii=False)

    @staticmethod
    def download(save_path, title, url):
        """
        download video and save to self-defined path
        :param save_path: save path (e.g., video/download)
        :param title: video name
        :param url: video url
        :return: bytes
        """
        rsp = requests.head(url)
        # get the file size
        size = rsp.headers['Content-Length']
        # convert Byte to MB
        print(title + ": %.2f MB" % (int(size) / 1024 / 1024))
        p = 0
        rp = requests.get(url, stream=True)
        path = "{}/{}.mp4".format(str(save_path).rstrip("/"), title)
        with open(path, 'wb') as f:
            # Start downloading 1024 bytes per request
            for i in rp.iter_content(chunk_size=1024):
                p += len(i)
                f.write(i)
                done = 50 * p / int(size)
                sys.stdout.write("\r[%s%s] %.2f%%" % ('█' * int(done), '' * int(50 - done), done + done))
            sys.stdout.flush()
        print("\n")


def logPrintMsg(path):
     pornhub = PornHub(path)
     urls = json.loads(pornhub.get_video())
     return urls['quality'][0]['videoUrl']


if __name__ == '__main__':
    pornhub = PornHub("https://www.pornhub.com/view_video.php?viewkey=ph5f51501da477d")
    urls = json.loads(pornhub.get_video())
    print(urls)
    # pornhub.download("./pornhubs/", urls["title"], urls["quality"][-1]["videoUrl"])
    # pornhub.get_keys("mv", 1, 5)