from pornhub_api import PornhubApi


def search(key):
    api = PornhubApi()
    test = api.search.search(key)
    return test


