from pornhub_api import PornhubApi


def search():
    api = PornhubApi()
    test = api.search.search("beautiful")
    return test


