import { del, get, put } from '../../services/http';

function envelope(data) {
  return { data: { code: 1, data } };
}

export function mapUser(user) {
  return {
    ...user,
    id: user.userId,
    userName: user.nickname,
    avatarAddress: user.avatarUrl,
    backgroundAddress: user.backgroundUrl,
    introduce: user.signature || '',
    grade: user.level,
    coinNumber: user.coinBalance,
    collectNumber: user.favoriteCount,
    videoNumber: user.videoCount,
    dynamicNumber: user.postCount,
    ownDynamicNumber: user.postCount,
    createTime: user.createdAt,
    adminFlag: user.role === 1,
    followNumber: user.followingCount,
    fansNumber: user.followerCount,
    likeNumber: user.likeReceived,
    playNumber: user.playTotal,
    isFollowFlag: user.followed ? 1 : 0,
    publicCollect: user.showFavoriteList ? 1 : 0,
    publicFollowList: user.showFollowList ? 1 : 0,
    publicFansList: user.showFansList ? 1 : 0,
    publicCoin: 0,
    publicLove: 0,
    publicBirthday: 0,
    publicAnime: 0,
  };
}

export async function selectUserInfo(id) {
  return envelope(mapUser(await get(`/auth/user/${id}`)));
}

export async function putUserInfo(token, user) {
  return envelope(await put('/auth/profile', { signature: user.introduce }));
}

export async function putHomeSettings(token, user) {
  return envelope(await put('/auth/privacy', {
    showFavoriteList: user.publicCollect === 1,
    showFollowList: user.publicFollowList === 1,
    showFansList: user.publicFansList === 1,
  }));
}

export async function addFollow(token, userId, targetId) {
  return envelope(await put(`/user/${targetId}/follow`));
}

export async function deleteFollow(token, userId, targetId) {
  return envelope(await del(`/user/${targetId}/follow`));
}

export async function fansOrFollow(homeUserId, userId, type, pageNum, keyWord) {
  const page = await get('/home/relations', { homeUserId, type, pageNum, keyWord });
  return envelope({ ...page, records: page.records.map(mapUser) });
}
