from pornhub_api import PornhubApi
from java import jclass


def search(key, page):
    api = PornhubApi()
    search = api.search.search(key, page=page, thumbsize="large")
    videos = search.videos
    PornHubVideoGroupInfo = jclass("com.epiphany.callshow.model.PornHubVideoGroupInfo")
    PornHubVideoInfo = jclass("com.epiphany.callshow.model.PornHubVideoInfo")
    groupInfo = PornHubVideoGroupInfo()

    for i in videos:
        info = PornHubVideoInfo()
        info.setTitle(i.title)
        info.setDuration(i.duration)
        info.setVideo_id(i.video_id)
        videoPath = i.url.scheme + "://" + i.url.host + i.url.path + "?" + i.url.query
        info.setVideoRealPath(videoPath)  # download_pornhub.logPrintMsg(videoPath)
        imagePath = i.default_thumb.scheme + "://" + i.default_thumb.host + i.default_thumb.path
        info.setImagePath(imagePath)
        groupInfo.addItem(info)

    return groupInfo
