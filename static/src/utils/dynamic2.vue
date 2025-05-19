<template>
  <div v-show="dynamic.dynamicUserInfo2.id" class="dynamic">
      <div v-if="dynamic.dynamic.upFlag===1&&checkUpId===store.userId" class="up-sign">
            <svg class="bili-dyn-tag__icon" style="width: 12px; height: 12px;" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" viewBox="0 0 12 12" width="12" height="12"><path d="M9.8054 5.406475C10.198275 5.89755 9.84865 6.625 9.21975 6.625L2.780235 6.625C2.151355 6.625 1.80173 5.89755 2.194585 5.406475L5.41435 1.38177C5.714575 1.00647 6.2854 1.00647 6.58565 1.38177L9.8054 5.406475z" fill="currentColor"></path><path d="M4.375 5.5C4.375 5.085775 4.710785 4.75 5.125 4.75L6.875 4.75C7.289225 4.75 7.625 5.085775 7.625 5.5L7.625 9.75C7.625 10.164225 7.289225 10.5 6.875 10.5L5.125 10.5C4.710785 10.5 4.375 10.164225 4.375 9.75L4.375 5.5z" fill="currentColor"></path></svg>
            &nbsp;置顶
      </div>
      <div class="dynamic-content">
          <div class="avatar">
              <div class="avatar-container" @mouseover="selectFollowFlag(dynamic.dynamicUserInfo2)" @mouseleave="selectFollowDelayF">
                <img @click="openHome(1,dynamic.dynamicUserInfo2.id)" :src="dynamic.dynamicUserInfo2.avatarAddress">
                <userInfo2 class="user-info" :user-info="dynamic.dynamicUserInfo2"/>
              </div>
          </div>
          <div class="head">
              <div class="user-name"><span @click="openHome(1,dynamic.dynamicUserInfo2.id)">{{ dynamic.dynamicUserInfo2.userName }}</span></div>
              <div class="date"><span>{{ dynamic.video&&!dynamic.comment?dynamic.video.createTime:dynamic.publishTime }}</span><span v-if="!dynamic.comment&&dynamic.video">&nbsp;·&nbsp;投稿了视频</span></div>
          </div>
          <div v-if="dynamic.comment" class="body" @click="openDynamic(dynamic.dynamic.id)">
              <div class="content" ref="dynamicContentRef" :style="{'-webkit-line-clamp': showFlag? 12 : 6}">
                <div v-show="dynamic.dynamic.title" class="title">{{ dynamic.dynamic.title }}</div>
                <span v-html="dynamic.comment?dynamic.comment.content:dynamic.dynamic.content"></span>
              </div>
              <el-image
                v-if="dynamic.dynamic.imgAddress&&dynamic.dynamic.imgAddress.split(',').length>1"
                @click.stop 
                class="img"
                :hide-on-click-modal="true"
                :src="dynamic.dynamic.imgAddress.split(',')[0]"
                :zoom-rate="1.2"
                :max-scale="7"
                :min-scale="0.2"
                :preview-teleported="true"
                :preview-src-list="[dynamic.dynamic.imgAddress.split(',')[0]]"
                fit="cover"
               />
              <div v-if="contentShowFlag" class="show-more" @click.stop="showFlag=!showFlag">{{ showFlag? '收起' : '展开' }}</div>
              <div v-if="dynamic.video" class="video-info-container" @click.stop>
                   <div class="up-info">
                       <div class="left">
                           <div @click="openHome(1,dynamic.upUserInfo2.id)" class="up-avatar">
                               <img :src="dynamic.upUserInfo2.avatarAddress">
                               <userInfo2 @click.stop class="up-user-info" :user-info="dynamic.upUserInfo2"/>
                           </div>
                           <span @click="openHome(1,dynamic.upUserInfo2.id)" class="up-user-name">
                            {{ dynamic.upUserInfo2.userName }}
                            <userInfo2 @click.stop class="up-user-info" :user-info="dynamic.upUserInfo2"/>
                           </span>
                           <span class="end">投稿了视频</span>
                       </div>
                       <div class="right">
                            <div v-show="dynamic.upUserInfo2.isFollowFlag===0" class="follow" @click="followF(dynamic.upUserInfo2)">关注</div>
                            <div v-show="dynamic.upUserInfo2.isFollowFlag===1" class="unfollow" @click="deleteFollowDialogFlag=true">已关注</div>   
                        </div>
                   </div>
                   <videoInfo class="video-info" @click="openVideo(dynamic.video.videoId)" :video="dynamic.video"/> 
              </div>
              <div v-else class="delete-info-container">
                  <div class="delete-info">
                     <svg class="dyn-none__icon" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" viewBox="0 0 24 24" width="24" height="24"><path d="M9.83496 3.5C10.7972 1.83333 13.20285 1.83334 14.16505 3.5L21.7428 16.625C22.705 18.29165 21.5022 20.375 19.5777 20.375L4.42228 20.375C2.49778 20.375 1.29496 18.29165 2.25722 16.625L9.83496 3.5zM12 8.125C11.5858 8.125 11.25 8.46079 11.25 8.875L11.25 13.5C11.25 13.9142 11.5858 14.25 12 14.25C12.4142 14.25 12.75 13.9142 12.75 13.5L12.75 8.875C12.75 8.46079 12.4142 8.125 12 8.125zM12.75 16.125C12.75 15.7108 12.4142 15.375 12 15.375C11.5858 15.375 11.25 15.7108 11.25 16.125L11.25 16.5C11.25 16.9142 11.5858 17.25 12 17.25C12.4142 17.25 12.75 16.9142 12.75 16.5L12.75 16.125z" fill="currentColor"></path></svg>
                     <span>源动态已被作者删除</span>
                  </div>
              </div>
          </div>
          <div v-else-if="dynamic.video&&!dynamic.comment" class="body2">
            <videoInfo :video="dynamic.video"/>
          </div>
          <div v-else class="body" @click="openDynamic(dynamic.dynamic.id)">
              <div class="content" ref="dynamicContentRef" :style="{'-webkit-line-clamp': showFlag? 12 : 6}">
                <div v-show="dynamic.dynamic.title" class="title">{{ dynamic.dynamic.title }}</div>
                <span v-html="dynamic.comment?dynamic.comment.content:dynamic.dynamic.content"></span>
              </div>
              <el-image
                v-if="dynamic.dynamic.imgAddress&&dynamic.dynamic.imgAddress.split(',').length>1"
                @click.stop 
                class="img"
                :hide-on-click-modal="true"
                :src="dynamic.dynamic.imgAddress.split(',')[0]"
                :zoom-rate="1.2"
                :max-scale="7"
                :min-scale="0.2"
                :preview-teleported="true"
                :preview-src-list="[dynamic.dynamic.imgAddress.split(',')[0]]"
                fit="cover"
               />
              <div v-if="contentShowFlag" class="show-more" @click.stop="showFlag=!showFlag">{{ showFlag? '收起' : '展开' }}</div>
          </div>
          <div v-if="dynamic.replyComment||dynamic.userInfo2s?.length>0" class="comment">
              <div class="comment-content">
                  <img v-if="dynamic.userInfo2s?.length>0" class="comment-icon" src="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAADYAAAA2CAYAAACMRWrdAAAACXBIWXMAACE4AAAhOAFFljFgAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAR6SURBVHgB7Vpdcts2EN6FqMRvZU8Q+wR1T5DkBu4JZM60tdXYjXWCSL2AnDYeT9IHSieoc4HauYF7Ais9gfPmVhK2C5qSABDQj0nRUkffTBSBAgl8XOxi94MBNthgJYDwCPjtPH7B/71IGgKujw+iCygYpRJrn8fb1QrGQCmpFATQ489GkQRLI6ZIBQIvecBtXx9Rwf2fvq91oQAIKAkBQFMnxVa6Vf/0PnJIp+oFQAEohZiaLAqsjdpM6GLwhHaOD/a/JkktrWvIS/UNFIBSiClr6e2BpEYjihJrHdejJpObLD+CvXYch5AT5SxFgc/H34k+NupRz+rR0b6H1X/Ec8g7JCwZv57H+2bAELHdh612Bbq/odyBnFg6Mfatsc+osH50WPvo6qcHEpKw2kvx7EP3tW4tBGz6+nK/MRkUZrR8CJZGTEVCSdQctRNrHbj3qDRYTKxE4gZyYinEtM14YoUp1gr+NTMRFPIz5EThxFwZBhG99VkrgYQ9rXX76sfoGnKiUGIquc2kTchJ7mF0MvVGfTtAKCRfDObppHygcgd73jwPIeTo942d3CpS/Sq9nPZs9TKs527ztSbMAApx05fyk2NPTIeeAkXoyUC8lpJOdH+ZCwRX/af03SjD8IFJdPR0a1Hwvc1XP9Ra9nWvxRSpah8v2T92FykB1H6EQK2jw+h0rhsEfgU5wOlY8937+MvRgTmelxiT+oNnuWs8RG2ipGcIvAQ1S6qEdrAFp7OspEPljVXEkKaUMy4g6v3xDRuio4/rNIZKg4TAceqTFIKSojT1GSPxDw4Wo3ZF0k7ds+aLhj1Hdpfo53rUGbWdUZELPr3E6PFbfWmTemwoEveV9z3swOYO91p0Y3952yjJCovCSNesNCxDzK5gOSHNvVkuA5mazUrDMsQC26SV/AnpMhDcWYGN5Be9PTPzEMPVJMYzNyzG0fja/NmCHXbrK+pfnF/qFru1t5jSVKqigRVdbsjGgQwxSyLrwYqCSJ8n/WX/vpYWS+Q8ozIXl3aftSQWCDMi9kn+PyymF6bKXVwJxHoS0wpTToavnF1gzdD+EO8aeaFEZ8Xt2sd6o++4YClRBoIhGDJD/6n85Oq3fkvR1Ec6vtovu49ZWXIRBwRFISOXe5ahQtZi0iS2dQcrQ8ySy298crlChtjAyjYGK+JntprFAmxrWv+sxbZMi9GqBBBLqlDS29Tu9oXUGSeiCJq7/GMg61vUnVXVu6MiTrJlRKwVdS78ELTP4m/Zt9qjtvKtgXlQ6ISTGA3Nc2ElW797332wqPkQnPPLPPu92wwC/NM63GjNo8F4tVCXQpsezt3y8uyll0Jde+SHXfDHeBnLIX3WRgqFRxzV/DjkciQ5UnIpzyzenvI5QAPmgFcw5VL7pNqHZ6ZidT8okDug8AT31Me4v0BrYjATnjdN6sRmXlJTnjNBckDAlnuE9IrSzysm9cuiuubcsrz+908C8Zn2k5KnQ8/MtscDoSWPTyagR2FWyenv9N7ecAsuFpHLN9hggw028OE/ENKxBsvdpsEAAAAASUVORK5CYII="> 
                  <img v-else-if="dynamic.replyComment" class="comment-icon" src="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAADYAAAA2CAYAAACMRWrdAAAACXBIWXMAACE4AAAhOAFFljFgAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAASmSURBVHgB7VlRctpIEO0eBMknuQF7giUnWHyCeE8A+tg4LkhsThB8Ary7drni/RCcIPgEZk8QbpDkBnymjJhOj4xgJI1AQkKQKt4PSDUzmjfd093zBuCII4444ogVEHLG3Z1TcwFqafpYAN/Oz+1vkCMyE/v3zmnwKKco8A8gqEMWIExI0v88zuj9uT2GDNia2D93TkuUsMmTaMAOQGxFBOx1zppD2AKpiSkLYQn7ma2TEIqgK+mkm9JVUxG7/W/4kV2lB9GPTwXBWAKOAeV3S8IEEkJKqEoBVUDxuwBqSIQGT6oabseu3mv/1bxKOm5iYjf3A4cZtPR3ajWB8Np9IYdd255CDug7TrX0w9uzHzEchBAGnbctO8k4iYgZSUm6cl/CdV6ETGC37ymCgZcJyW0kFnY/z0qS7KxRKylub526tPBzyHrXnbNWd12/tcS8yCfQ8Z+33chZ0efcaAl81MnxYp+sW9xYYsrXrSf8og82k/Rb0aR8KMuRhY+wCCzeIlfoddxWEHEDVVxxETQ/dfdFSqHdticSaBkV1dysH3AZ1z6WmJTU8v+r1emc2dewZ3xQc0AY+88cWC6UZ5naGompvRWwFgcLOBDQnPRcVi0/BaO1DyMxr1TyB2JrFRUBk2Axl1UBgPjG1M7sinr9J2mrWm2X4Ij4sHqAuskdI8S8aj2IMRwextr/amUWPSZFLSaCdRpXF4nrvqIQntN8Hi3Io8RkoNF0lyXTtljMaTkvIWCzK+ogrfOhQZ8byZTEfmVEiHlF7gKYUrsoEvrcUEQ9K0Is3EiJM3Bg6N87gWBBhoNthJgbajSD3WgaWVAKRUFX8zIfEWKq0NXdsSTMmX2fQG2x1VxNxXlM5bHK7NyxEVdo7gPqbMbF77LkQzQXEHEl1Uh7qq47HhSNiiWa+vMsWBQvEXvQZJ3jUasZp3zIfL3P85iCslZZ4NflC1bGOu9aJ6a2sXkscjworSSCfYHn8Fl/RkGxukcsMXU8IKK/ly/YejefBn3YE5SoFBBpWa1qv7Vj69i1lYf7AnoUDKWX3gcKhkkpi9tbPjbLbyERZdFp9CR3r4GoaFyesftRILxP3QT7fWOtqEQUdElt0FXRCXCq5LCbT8Mm7Ai398MLVsm+Rki5yeS/xBJ3jHDpuQVrjwOuax7W+Xyib6hSCcUbFpIuw/q9J7e59Ge3newbqS4lPOESoKcnyNDHp5wwJ0gwZals+P7MHsWOxQtVKePFol+dCGqmy4jFwOMZkZ3G9be6H/NULNOlQQizCr0yHVRNyq4Jz4GLuusWKA6ZbjQ9CRzZehhTKBOedt41H/RXJoU52g9UqrnKoo5ZkAEfzu0B/wyeJ8vkWFbQb0ckyVfhPl6U0zV4tUfV3RrRdw5lE7cC4zzkiEwWM4GTOPn/OQjYC/J+6H7Uk+wm/T0LMlksKbw99eTlwpr/bnlzsyOxKFeLeffTwiPwDN5jWIJ6OHwXcR2Vr8UQTvVHQhrwvqtisM3EnXM+2nHVkq9KFdLRI0mWi+pZuZiLw1wtti7BZg3faZErMXUxJwBXR5s9EPKRe7hXAYSDQ23+EkaHKI8fcUTB+Akpbi5uawt9pAAAAABJRU5ErkJggg==">
                  <div v-if="dynamic.replyComment" class="comment-text">
                      <span class="user-name" @click="openHome(1,dynamic.replyComment.userId)" @mouseover="selectFollowFlag(dynamic.replyUserInfo2)" @mouseleave="selectFollowDelayF">{{ dynamic.replyComment.userName }}：<userInfo2 class="user-info" :user-info="dynamic.replyUserInfo2"/></span>
                      <span class="user-content2" @click="openVideoOrDynamic(dynamic.replyComment,dynamic.replyComment.id)" v-html="cleanColor(dynamic.replyComment.content)"></span>
                  </div>
                  <div v-else-if="dynamic.userInfo2s?.length>0" class="comment-text">
                    <span class="user-name" @click="openHome(1,user.id)" @mouseover="selectFollowFlag(user)" @mouseleave="selectFollowDelayF" v-for="(user,index) in dynamic.userInfo2s" :key="user.id">{{ user.userName }} <userInfo2 class="user-info" :user-info="user"/><span v-if="index!=dynamic.userInfo2s.length-1">、</span></span>
                    <span class="user-content">&nbsp;{{ getLikeNumber(dynamic) }}</span>
                  </div>
              </div>
          </div>
          <div class="fotter">
              <div class="share" :style="{color:store.shareHover&&shareOpenFlag?'#00AEEC':'#61666D'}"  @click="openShare(dynamic)" @mouseover="store.shareHover=true" @mouseleave="store.shareVideoFocusFlag?store.shareHover=true:(store.shareHover=false,shareOpenFlag=false)">
                <svg style="width: 18px; height: 18px;" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" viewBox="0 0 18 18" width="18" height="18"><path d="M9.789075 2.2956175C8.97235 1.6308450000000003 7.74999 2.212005 7.74999 3.26506L7.74999 5.3915500000000005C6.642015000000001 5.5780325 5.3073725 6.040405 4.141735000000001 7.11143C2.809155 8.335825 1.751515 10.3041 1.45716 13.404099999999998C1.409905 13.9018 1.7595399999999999 14.22505 2.105415 14.317499999999999C2.442215 14.40755 2.8807175 14.314625 3.127745 13.92915C3.9664525 12.620249999999999 4.89282 11.894575 5.765827499999999 11.50585C6.4628049999999995 11.19545 7.14528 11.093125 7.74999 11.0959L7.74999 13.235025C7.74999 14.2881 8.97235 14.869250000000001 9.789075 14.2045L15.556199999999999 9.510425000000001C16.355075 8.860149999999999 16.355075 7.640124999999999 15.556199999999999 6.989840000000001L9.789075 2.2956175zM9.165099999999999 3.0768275000000003L14.895025 7.739050000000001C15.227975 7.980475 15.235775 8.468875 14.943874999999998 8.7142L9.17615 13.416800000000002C8.979474999999999 13.562024999999998 8.75 13.4269 8.75 13.227375000000002L8.75 10.638175C8.75 10.326975000000001 8.542125 10.134725 8.2544 10.1118C7.186765 10.02955 6.1563175 10.2037 5.150895 10.69295C4.14982 11.186925 3.2102250000000003 12.096525 2.573625 13.00995C2.54981 13.046975 2.52013 13.046025 2.5211725 12.986C2.8971525 10.0573 3.9373475 8.652125 4.807025 7.85305C5.87747 6.8694775 7.213197500000001 6.444867500000001 8.2272 6.33056C8.606525 6.287802500000001 8.74805 6.0849325 8.74805 5.7032275L8.74805 3.2615475C8.74805 3.0764875000000007 8.993175 2.9321925 9.165099999999999 3.0768275000000003z" fill="currentColor"></path></svg>
                 &nbsp;{{ !dynamic.comment&&dynamic.video?dynamic.video.videoShareNumber:dynamic.dynamic.shareNumber }} 
                 <videoShare v-show="store.shareHover&&shareOpenFlag" class="video-share" @click.stop :height-flag="true" :open-flag="shareOpenFlag" :dynamic="dynamic"/>
              </div>
              <div class="comment4" @click="openComment(dynamic)" :style="{color:store.commentOpenFlag===dynamic.dynamic.id?'#00AEEC':'#61666D'}">
                <svg style="width: 18px; height: 18px;" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" viewBox="0 0 18 18" width="18" height="18"><path d="M1.5625 7.875C1.5625 4.595807499999999 4.220807499999999 1.9375 7.5 1.9375L10.5 1.9375C13.779175 1.9375 16.4375 4.595807499999999 16.4375 7.875C16.4375 11.0504 13.944675 13.6435 10.809275 13.80405C10.097025 14.722974999999998 8.920875 15.880675 7.267095 16.331325C6.9735075 16.4113 6.704762499999999 16.286224999999998 6.55411 16.092325C6.40789 15.904149999999998 6.3561 15.634350000000001 6.4652449999999995 15.383025C6.72879 14.776249999999997 6.776465 14.221025000000001 6.7340175 13.761800000000001C3.8167675 13.387125 1.5625 10.894475 1.5625 7.875zM7.5 2.9375C4.773095 2.9375 2.5625 5.148095 2.5625 7.875C2.5625 10.502575 4.61524 12.651075000000002 7.2041924999999996 12.8038C7.4305875 12.817174999999999 7.619625000000001 12.981200000000001 7.664724999999999 13.203475C7.772575 13.734575000000001 7.8012 14.405425000000001 7.5884275 15.148399999999999C8.748325 14.6682 9.606 13.759825 10.151275 13.016475C10.24445 12.889475 10.392050000000001 12.8138 10.54955 12.812275C13.253575 12.785725 15.4375 10.58535 15.4375 7.875C15.4375 5.148095 13.226899999999999 2.9375 10.5 2.9375L7.5 2.9375z" fill="currentColor"></path></svg>
                &nbsp;{{ !dynamic.comment&&dynamic.video?dynamic.video.videoCommentNumber:dynamic.dynamic.commentNumber }}
              </div>
              <div class="like" @click="likeDynamicF(dynamic)" :style="{color:dynamic.likeFlag===1?'#00AEEC':'#61666D'}">
                <svg v-show="dynamic.likeFlag===0" style="width: 18px; height: 18px;" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" viewBox="0 0 18 18" width="18" height="18"><path d="M10.4511 2.2220125C10.218425 2.194885 10.002175 2.2953725 9.884175 2.433395C9.4264 2.9688525 9.321875 3.7501399999999996 8.978575 4.3581725C8.533574999999999 5.146395 8.1198 5.6213375 7.609775000000001 6.068507499999999C7.1751375 6.449565 6.738407499999999 6.697442499999999 6.3125 6.8050575L6.3125 14.854575C6.9198900000000005 14.868174999999999 7.572900000000001 14.876875 8.25 14.876875C9.936425 14.876875 11.367025 14.823325 12.33115 14.773699999999998C13.03235 14.737575 13.646025000000002 14.390075 13.966750000000001 13.81945C14.401900000000001 13.04535 14.9387 11.909650000000001 15.264174999999998 10.571200000000001C15.56665 9.327275 15.704699999999999 8.304325 15.766675 7.582224999999999C15.7988 7.208262500000001 15.50165 6.875019999999999 15.059999999999999 6.875019999999999L11.323274999999999 6.875019999999999C11.156575 6.875019999999999 11.000800000000002 6.791952499999999 10.907975 6.653499999999999C10.783725 6.468192500000001 10.82855 6.2670175 10.9037 6.07485C11.059 5.675084999999999 11.29355 4.9974475 11.382425000000001 4.4018275C11.470875000000001 3.80917 11.450999999999999 3.32219 11.212050000000001 2.86913C10.9571 2.3857825 10.66065 2.2464475 10.4511 2.2220125zM12.034300000000002 5.87502L15.059999999999999 5.87502C16.02035 5.87502 16.850875 6.64489 16.763 7.667825C16.697100000000002 8.435525 16.55155 9.5092 16.235825000000002 10.807500000000001C15.882625 12.259950000000002 15.3035 13.482225 14.838450000000002 14.309474999999999C14.32695 15.2194 13.377475 15.721150000000002 12.38255 15.772375C11.405125 15.822725 9.956949999999999 15.876875000000002 8.25 15.876875000000002C6.5961925 15.876875000000002 5.0846825 15.826025000000001 4.0136674999999995 15.77715C2.8370825 15.723474999999999 1.8519999999999999 14.850000000000001 1.725645 13.654824999999999C1.6404649999999998 12.849274999999999 1.5625 11.80725 1.5625 10.689375C1.5625 9.665175000000001 1.6279400000000002 8.736175 1.7045524999999997 7.998975C1.8351224999999998 6.7427075 2.9137075 5.87502 4.130655 5.87502L5.8125 5.87502C6.072015 5.87502 6.457235 5.7490675 6.9505175 5.316582499999999C7.377705000000001 4.942045 7.7193000000000005 4.5546075 8.107775 3.8665374999999997C8.492075 3.18585 8.605825 2.389785 9.124075 1.783595C9.452975 1.3988800000000001 9.99475 1.162025 10.5669 1.228745C11.16225 1.29816 11.717425 1.683875 12.09655 2.4025825000000003C12.478275 3.1262375000000002 12.474075 3.8618225 12.371500000000001 4.54938C12.302149999999997 5.0139949999999995 12.155425000000001 5.510059999999999 12.034300000000002 5.87502zM5.3125 14.82705L5.3125 6.875019999999999L4.130655 6.875019999999999C3.3792199999999997 6.875019999999999 2.77211 7.400795 2.6991975000000004 8.10235C2.6253525 8.812875 2.5625 9.70665 2.5625 10.689375C2.5625 11.762875 2.6374975 12.768475 2.7200975 13.549700000000001C2.7919925 14.229675 3.3521950000000005 14.74595 4.05924 14.778224999999999C4.4278775 14.795 4.849985 14.812050000000001 5.3125 14.82705z" fill="currentColor"></path></svg>
                <svg v-show="dynamic.likeFlag===1" class="svg2" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" viewBox="0 0 18 18" width="18" height="18" style="width: 18px; height: 18px;"><path d="M15.238949999999999 5.8749875L11.728124999999999 5.8749875C11.903625 5.509125 12.108450000000001 5.0146725 12.179124999999999 4.54079C12.225925 4.227235 12.230549999999997 3.8411524999999997 12.192600000000002 3.4729099999999997C12.155475 3.1126975 12.072274999999998 2.715405 11.909324999999999 2.4065275C11.5572 1.73896 11.07925 1.2830650000000001 10.515075 1.1730275000000001C9.9175 1.056475 9.4 1.3591199999999999 9.086975 1.832795C8.821325 2.2348025 8.71795 2.5693425000000003 8.62185 2.8804925L8.619125 2.8893475C8.526275 3.1897624999999996 8.4337 3.488995 8.19635 3.9093850000000003C7.807925000000001 4.59742 7.489369999999999 4.956485000000001 7.062139999999999 5.331055C6.807695000000001 5.5541375 6.541364999999999 5.6883925 6.3125 5.760025L6.3125 15.85475C6.9202625 15.868200000000002 7.573125 15.876800000000003 8.25 15.876800000000003C10.00675 15.876800000000003 11.4894 15.819474999999999 12.466925 15.767950000000003C13.408750000000001 15.7183 14.305975 15.243900000000002 14.795475 14.385325C15.267499999999998 13.557499999999997 15.871775 12.304749999999999 16.235825000000002 10.807475C16.577575000000003 9.40205 16.719975 8.259725 16.7778 7.4839150000000005C16.846225 6.565215 16.10015 5.8749875 15.238949999999999 5.8749875zM5.3125 15.827525L5.3125 5.8749875L3.9767325000000002 5.8749875C2.8486374999999997 5.8749875 1.8491449999999998 6.6792625 1.7212225 7.843025000000001C1.63766 8.60325 1.5625 9.5917 1.5625 10.6893C1.5625 11.876325000000001 1.6504175 12.977975 1.7415649999999998 13.801975C1.864035 14.909174999999998 2.7766325 15.718875 3.8673275 15.770325C4.28143 15.789874999999999 4.769835 15.810149999999998 5.3125 15.827525z" fill="currentColor"></path></svg>
                &nbsp;{{ !dynamic.comment&&dynamic.video?dynamic.video.videoLikeNumber:dynamic.dynamic.likeNumber }}
              </div>
          </div>
          <div class="up-btn" @mouseover="selectFollowFlag(dynamic.dynamicUserInfo2)" @mouseleave="selectFollowDelayF">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><path fill-rule="evenodd" clip-rule="evenodd" d="M13.7485 5.49841C13.7485 6.46404 12.9657 7.24683 12.0001 7.24683C11.0344 7.24683 10.2516 6.46404 10.2516 5.49841C10.2516 4.53279 11.0344 3.75 12.0001 3.75C12.9657 3.75 13.7485 4.53279 13.7485 5.49841ZM13.7484 18.4982C13.7484 19.4639 12.9656 20.2467 11.9999 20.2467C11.0343 20.2467 10.2515 19.4639 10.2515 18.4982C10.2515 17.5326 11.0343 16.7498 11.9999 16.7498C12.9656 16.7498 13.7484 17.5326 13.7484 18.4982ZM11.9999 13.7487C12.9656 13.7487 13.7484 12.9658 13.7484 12.0002C13.7484 11.0345 12.9656 10.2517 11.9999 10.2517C11.0343 10.2517 10.2515 11.0345 10.2515 12.0002C10.2515 12.9658 11.0343 13.7487 11.9999 13.7487Z" fill="currentColor"></path></svg>
            <div v-if="dynamic.dynamicUserInfo2.id!==store.userId" class="up-info">
               <div @click="dynamic.dynamicUserInfo2?.isFollowFlag?unFollowF(dynamic.dynamicUserInfo2):followF(dynamic.dynamicUserInfo2)">{{ dynamic.dynamicUserInfo2?.isFollowFlag?'取消关注':'关注' }}</div>
            </div>
            <div v-else class="up-info2">
               <div @click="putUpStatusF(dynamic.dynamic)">{{ dynamic.dynamic.upFlag===1?'取消置顶':'置顶' }}</div>
               <div @click="deleteDynamicDialogFlag=true">删除</div>
            </div>
          </div>
      </div>
      <comment4 v-if="dynamic.video&&!dynamic.comment&&store.commentOpenFlag===dynamic.dynamic.id" :dynamic="dynamic"/>
      <comment5 v-if="(dynamic.comment||!dynamic.dynamic.videoId)&&store.commentOpenFlag===dynamic.dynamic.id" :dynamic="dynamic"/>
      <el-dialog
      v-model="deleteFollowDialogFlag"
      append-to-body
      width="320"
      :lock-scroll="false"
      align-center
      style="height: 152px; border-radius: 12px"
    >
      <div class="delete-dialog-title">取消关注</div>
      <div class="delete-dialog-content">
        真的要取消对 {{ dynamic.upUserInfo2.userName }} 的关注吗？
      </div>
      <div class="delete-dialog-btn-container">
        <button class="delete-dialog-cancel-btn" @click="deleteFollowDialogFlag=false">
        点错了~
      </button>
      <button
        class="delete-dialog-confirm-btn"
        @click="unFollowF(dynamic.upUserInfo2)"
      >
        取消关注
      </button>
      </div>
      </el-dialog>
      <el-dialog
      v-model="deleteDynamicDialogFlag"
      append-to-body
      width="320"
      :lock-scroll="false"
      align-center
      style="height: 152px; border-radius: 12px"
    >
      <div class="delete-dialog-title">要删除动态吗？</div>
      <div class="delete-dialog-content">
        动态删除后将无法恢复，请谨慎操作
      </div>
      <div class="delete-dialog-btn-container">
        <button class="delete-dialog-cancel-btn" @click="deleteDynamicDialogFlag=false">
        取消
      </button>
      <button
        class="delete-dialog-confirm-btn2"
        @click="deleteDynamicF(dynamic)"
      >
        确认删除
      </button>
      </div>
      </el-dialog>    
  </div>
</template>

<script setup>
import { useGlobalStore } from '../store/store';
import { ElMessage } from "element-plus";
import videoInfo from './videoInfo.vue';
import userInfo2 from './userInfo2.vue';
import {putUpStatus,likeDynamic,deleteDynamic} from '@/api/dynamic/index'
import {addFollow,deleteFollow,checkFollow} from '../api/user/index';
import { onMounted, ref,computed } from 'vue';
import videoShare from './videoShare.vue';
import comment4 from '@/comment/comment4.vue';
import comment5 from '@/comment/comment5.vue';

const props = defineProps({
    dynamic:{
        type: Object,
        required: true
    },
    checkUpId:{
        type: Number,
        required: true
    }
})

const store = useGlobalStore();
const deleteFollowDialogFlag = ref(false);
const deleteDynamicDialogFlag = ref(false);
const shareOpenFlag = ref(false);
const showFlag=ref(false);
const dynamicContentRef = ref(null);
const contentShowFlag = ref(false);


onMounted(()=>{

  let upVideo=props.dynamic.video;
  if(upVideo){
    store.selectUpVideo.upVideo=upVideo;
  }

  let onceFlag=null;
  onceFlag=setInterval(()=>{
    if(dynamicContentRef.value&&dynamicContentRef.value.scrollHeight>dynamicContentRef.value.clientHeight){
      contentShowFlag.value=true;
      clearInterval(onceFlag);
    }else if(dynamicContentRef.value&&dynamicContentRef.value.scrollHeight<dynamicContentRef.value.clientHeight){
      contentShowFlag.value=false;
      clearInterval(onceFlag);
    }
  },100)

})


//返回点赞用户信息
const getLikeNumber = computed(()=>{

return (dynamic)=>{
 if(dynamic.video){
 
 if(dynamic.video.videoLikeNumber>2)
    return `等${dynamic.video.videoLikeNumber+video.videoShareNumber}人点赞了`;
 else
    return `点赞了`;
}else{
    
 if(dynamic.dynamic.likeNumber>2)
    return `等${dynamic.dynamic.likeNumber+dynamic.dynamic.shareNumber}人点赞了`;
 else
    return `点赞了`;
}
}
})

//打开我的主页
function openHome(menu,id){
  window.open(
  `./home?homeMenu=${menu}&userId=${id}`,
  "_blank",
);
}

// 关注
async function followF(user){

if(store.userId===null)
{
    store.setLoginDialogVisibleFlag(1);   
    return;
}

if(store.userId===user.id){
    ElMessage({
        message: "不能关注自己(ง •̀_•́)ง",
        type: "info",
        plain: true,
        duration: 1700,
    });
    return;
}
await addFollow(store.token,store.userId,user.id).then(res=>{
    if(res.data.code===1){
        user.isFollowFlag=1;
        user.fansNumber++;
        ElMessage({
        message: "关注成功(ง •̀_•́)ง",
        type: "info",
        plain: true,
        duration: 1700,
      });
    }else{
        ElMessage({
        message: "关注失败(ง •̀_•́)ง",
        type: "info",
        plain: true,
        duration: 1700,
        });
    }
})
}

// 取消关注
async function unFollowF(user){

if(store.userId===null)
{
    store.setLoginDialogVisibleFlag(1);   
    return;
}

await deleteFollow(store.token,store.userId,user.id).then(res=>{
    if(res.data.code===1){
        user.isFollowFlag=0;
        ElMessage({
        message: "取消了对 "+user.userName+" 的关注",
        type: "info",
        plain: true,
        duration: 1700,
      });
      user.fansNumber--; 
      deleteFollowDialogFlag.value=false;
    }else{
        ElMessage({
        message: "取消关注失败(ง •̀_•́)ง",
        type: "info",
        plain: true,
        duration: 1700,
        });
    }
})
}

//查询是否关注
let selectFollowDelay=true;
function selectFollowFlag(user){
    if(!store.userId||!selectFollowDelay)
     return;
     selectFollowDelay=false;
     checkFollow(store.userId,user.id,store.token).then(res=>{
        if(res.data.code===1){
            user.isFollowFlag=res.data.data;
        }
     })
}

let selectFollowDelayTimer=null;
function selectFollowDelayF(){
    clearTimeout(selectFollowDelayTimer);
    selectFollowDelayTimer=setTimeout(()=>{
        selectFollowDelay=true;
    },500)
}

//打开视频页
function openVideo(videoId){
  window.open("./video?videoId=BV"+videoId,"videoWindow");
}

//打开视频或动态
function openVideoOrDynamic(comment,commentId){
  if(comment.videoId)
    window.open("./video?videoId=BV"+comment.videoId+'&commentId='+commentId,"videoWindow");
  else
    window.open(`./dynamicDetail?dynamicId=${comment.dynamicId}&commentId=${commentId}`,"dynamicWindow");
}

//点赞动态
function likeDynamicF(dynamic){
  if(!store.userId)
  {
    store.setLoginDialogVisibleFlag(1);   
    return;
  }

  likeDynamic(dynamic.dynamic.id,store.userId,store.token).then(res=>{
    if(res.data.code===1){

      if(!dynamic.likeFlag)
      {
        ElMessage({
        message: "点赞成功",
        type: "info",
        plain: true,
        duration: 1700,
        });
        dynamic.likeFlag=1;
        if(!dynamic.comment&&dynamic.video)
        {
          dynamic.video.videoLikeNumber++;
        }
        else{
          dynamic.dynamic.likeNumber++;
        }  
      }
      else{
        dynamic.likeFlag=0;
        if(!dynamic.comment&&dynamic.video)
        {
          dynamic.video.videoLikeNumber--;
        }
        else{
          dynamic.dynamic.likeNumber--;
        }  
      }
    }else{
      ElMessage({
        message: "点赞失败(ง •̀_•́)ง",
        type: "info",
        plain: true,
        duration: 1700,
      });
    }
  })
}

//打开或关闭分享
function openShare(dynamic){
  if(!store.userId)
  {
    store.setLoginDialogVisibleFlag(1);   
    return;
  }
  if(!dynamic.video)
  {
    ElMessage({
        message: "暂不支持分享动态",
        type: "info",
        plain: true,
        duration: 1700,
        });
    return;
  }
  shareOpenFlag.value=!shareOpenFlag.value;
  if(!shareOpenFlag.value)
    return;
}

//清除颜色
function cleanColor(content){
  return content.replace(/style="[^"]*"/g, '');;

}

//打开评论框
function openComment(dynamic){
  if(!store.userId)
  {
    store.setLoginDialogVisibleFlag(1);   
    return;
  }
  store.setUpUserId(dynamic.dynamicUserInfo2.id);
  store.commentOpenFlag=store.commentOpenFlag===dynamic.dynamic.id?false:dynamic.dynamic.id;
}

//修改动态置顶状态
function putUpStatusF(dynamic){
  if(!store.userId)
  {
    store.setLoginDialogVisibleFlag(1);   
    return;
  }

  putUpStatus(dynamic.id,store.userId,store.token).then(res=>{
    if(res.data.code===1){
      dynamic.upFlag=dynamic.upFlag===1?0:1;
      if(dynamic.upFlag===1)
      {
        store.setUpFlag(dynamic.id);
      }
    }else{
      ElMessage({
        message: res.data.msg,
        type: "info",
        plain: true,
        duration: 1700,
        });
    }
  })

}

//删除动态
function deleteDynamicF(dynamic){
  if(!store.userId)
  {
    store.setLoginDialogVisibleFlag(1);   
    return;
  }
  deleteDynamic(dynamic.dynamic.id,store.userId,store.token).then(res=>{
    if(res.data.code===1){
      ElMessage({
        message: "删除成功",
        type: "info",
        plain: true,
        duration: 1700,
      });
    store.setDeleteFlag(dynamic.dynamic.id);
    }else{
      ElMessage({
        message: "删除失败(ง •̀_•́)ง",
        type: "info",
        plain: true,
        duration: 1700,
      });
    }
  })
}

//打开动态详情页
function openDynamic(dynamicId){
  window.open(`./dynamicDetail?dynamicId=${dynamicId}`,"dynamicWindow");
}

</script>

<style lang="scss" scoped>

*{
    margin: 0;
    padding: 0;
    box-sizing: border-box;
}

.dynamic{
    width: 100%;
    background-color: white;
    border-radius: 6px;
    font-weight: 400;
    min-width: 556px;
    position: relative;
    margin-bottom: 8px;
        .up-sign{
        left: 0px;  
        color: #00AEEC;
        display: flex;
        align-items: center;
        font-size: 12px;
        padding: 10px 20px 0;
        height: 28px;
        }
        .dynamic-content{
        padding: 0 20px 0 87px;
        position: relative;
             
            .avatar{
            align-items: center;
            display: flex;
            height: 86.4px;
            justify-content: center;
            left: 0;
            position: absolute;
            top: 0;
            width: 86.4px;

              .avatar-container{
                width: 48px;
                height: 48px;
                border-radius: 50%;
                  img{
                  width: 48px;
                  height: 48px;
                  border-radius: 50%;
                  object-fit: cover;
                  cursor: pointer;
                }
                  .user-info{
                  left: 50%;  
                  transition: all .3s ease;
                  transition-delay: 0.3s;  
                  opacity: 0;
                  visibility: hidden;
                }
              }
              .avatar-container:hover{
                .user-info{
                   transition-delay: 0.3s;
                   opacity: 1;
                   visibility: visible;
                }
              }
            }
            .head{
            height: 62px;
            padding-top: 16px;
           
            .user-name{
            align-items: center;
            display: flex;
            height: 22px;
            margin: 1px 0 2px;
            width: max-content;

              span{
                font-size: 17px;
                line-height: 32px;
                transition: color .3s ease;
                color: #18191C;
                cursor: pointer;
                font-weight: 550;
                transition: all .3s ease;
              }
            }
            .user-name:hover{
                span{
                    color: #40C5F1;
                }
            }
            .date{
            align-items: center;
            display: flex;
            padding-right: 120px;
            padding-top: 4px;
            
              span{
                color: #9499A0;
                font-size: 13px;
                line-height: 18px;
              }
            }
            }
            .body{
            margin-top: 8px;
            cursor: pointer;
                .content{
                font-size: 15px;
                word-break: break-word;
                -webkit-font-smoothing: antialiased;
                font-weight: 400;
                line-height: 25px;
                overflow: hidden;
                padding-right: 60px;
                -webkit-box-orient: vertical;
                display: -webkit-box;
                -webkit-line-clamp: 6;
                text-overflow: ellipsis;

                    .title{
                    font-weight: 600;
                    }
                }
                .img{
                margin-top: 12px;
                max-width: 360px;
                max-height: 280px;
                border-radius: 6px;
                object-fit: cover;
                background-color: #f1f2f3;
                }
                .video-info-container{
                background-color: #F6F7F8;
                border-radius: 6px;
                margin-top: 12px;
                padding: 20px;

                    .up-info{
                      align-items: center;
                      display: flex;
                      height: 20px;
                      justify-content: space-between;
                      margin-bottom: 8px;
                      width: 100%;

                        .left{
                          align-items: center;
                          display: flex;
                          flex: 1;

                            .up-avatar{
                              position: relative;
                              width: 20px;
                              height: 20px;
                              border-radius: 50%;
                              margin-right: 4px;

                                img{
                                width: 20px;
                                height: 20px;
                                border-radius: 50%;
                                object-fit: cover;
                                }
                                .up-user-info{
                                  opacity: 0;
                                  visibility: hidden;
                                  transition: all .3s ease;
                                  transition-delay: 0.3s;
                                  position: absolute;
                                  left: 50%;
                                }
                            }
                            .up-avatar:hover{
                               .up-user-info{
                                   transition-delay: 0.3s;
                                   opacity: 1;
                                   visibility: visible;
                               }
                            }
                            .up-user-name{
                              position: relative;
                              font-size: 14.5px;
                              line-height: 25px;
                              display: inline-block;
                              transition: color .3s ease;
                              color: #61666D;
                              margin-right: 4px;

                                .up-user-info{
                                    opacity: 0;
                                    visibility: hidden;
                                    transition: all .3s ease;
                                    transition-delay: 0.3s;
                                    position: absolute;
                                    left: 50%;
                                  }
                            }
                            .up-user-name:hover{
                              color: #797F87;

                                .up-user-info{
                                   transition-delay: 0.3s;
                                   opacity: 1;
                                   visibility: visible;
                                }
                            }
                            .end{
                              font-size: 14.5px;
                              line-height: 25px;
                              color: #9499A0;
                            }
                        }
                        .right{
                          flex-shrink: 0;
                          div{
                            border-radius: 4px;
                            color: #00AEEC;
                            cursor: pointer;
                            font-size: 12.5px;
                            font-weight: 500;
                            height: 18px;
                            line-height: 18px;
                            position: relative;
                            text-align: center;
                            user-select: none;
                          }
                          .follow:before{
                          background-image: url(data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMTYiIGhlaWdodD0iMTYiIGZpbGw9Im5vbmUiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+PHBhdGggZmlsbC1ydWxlPSJldmVub2RkIiBjbGlwLXJ1bGU9ImV2ZW5vZGQiIGQ9Ik03Ljk5OSAyLjI1YS42LjYgMCAwIDEgLjYuNlY3LjRoNC41NTFhLjYuNiAwIDEgMSAwIDEuMkg4LjU5OXY0LjU1YS42LjYgMCAxIDEtMS4yIDBWOC42SDIuODVhLjYuNiAwIDAgMSAwLTEuMmg0LjU0OVYyLjg1YS42LjYgMCAwIDEgLjYtLjZaIiBmaWxsPSIjMDBBRUVDIi8+PC9zdmc+);
                          background-position: 50%;
                          background-repeat: no-repeat;
                          background-size: cover;
                          content: "";
                          height: 16px;
                          left: -18px;
                          position: absolute;
                          top: 1px;
                          width: 16px;
                          z-index: 1;
                          }
                          .unfollow{
                            color: #9499A0;
                          }
                        }
          
                    }
                    .video-info{
                      margin-top: 12px;
                    }
                }
                .delete-info-container{
                  background-color: #F6F7F8;
                  border-radius: 6px;
                  margin-top: 12px;
                  padding: 8px 12px;
                  
                  .delete-info{
                    align-items: center;
                    color: #9499A0;
                    display: flex;
                    font-size: 13px;
                    line-height: 18px;
                    text-indent: 6px;
                      
                      svg{
                      height: 16px;
                      vertical-align: -2px;
                      width: 16px;
                      }
                    }
                }
                .show-more{
                cursor: pointer;
                font-size: 15px;
                color: #008ac5;
                transition: color .3s ease;
                user-select: none;
                }
                .show-more:hover{
                 color: #00aeec; 
                }
            }
            .body2{
            margin-top: 12px;
            }
            .comment{
            margin-top: 12px; 
            padding-right: 30px;
                .comment-content{
                  position: relative;
                  display: -webkit-box;
                  -webkit-box-orient: vertical;
                  -webkit-line-clamp: 2;
                  overflow: hidden;

                    img{
                      user-select: none;
                    }

                    .comment-icon{
                        height: 14px;
                        left: 6px;
                        position: absolute;
                        top: 4px;
                        width: 14px;
                    }
                    .comment-text{
                        margin-left: 24px;
                        line-height: 22px;
                        font-size: 13px;
                        .user-name{
                            position: relative;
                            color: #61666d;
                            transition: color .3s ease;
                            cursor: pointer;

                              .user-info{
                                  opacity: 0;
                                  visibility: hidden;
                                  transition: all .3s ease;
                                  transition-delay: 0.3s;
                                  position: absolute;
                                  left: 50%;
                              }
                        }
                        .user-name:hover{
                            color: #00aeec;
                            
                              .user-info{
                                   transition-delay: 0.3s;
                                   opacity: 1;
                                   visibility: visible;
                              }
                        }
                        .user-content{
                          word-break: break-word;
                          color: #9499a0;
                        }
                        .user-content2{
                          word-break: break-word;
                          color: #9499a0;
                        }
                    }
                }
                .comment-content::before{
                background-color: #E3E5E7;
                border-radius: 1px;
                content: "";
                display: inline-block;
                height: 100%;
                left: 0;
                position: absolute;
                top: 0;
                width: 2px;
                }
            }
            .fotter{
            display: flex;
            height: 50px;
            justify-content: space-between;
            padding-right: 20px;

              .like,.share,.comment4{
                align-items: center;
                color: #61666D;
                cursor: pointer;
                display: flex;
                font-size: 13px;
                height: 100%;
                position: relative;
                user-select: none;
                transition: color .3s ease;
                width: 104px;

                .svg2{
                  color: #00AEEC;
                }
              }
              .like:hover,.share:hover,.comment4:hover{
                color: #00AEEC !important;
              }
              .share{
               position: relative; 
                   .video-share{
                    position: absolute;
                    bottom: -260px;
                   }
              }
            }
            .up-btn{
            position: absolute;
            right: 12px;
            top: 27px;
            border-radius: 4px;
            user-select: none;
            height: 24px;
            background-color: white;
            width: 24px;
            transition: all .3s ease;
            cursor: pointer;

                svg{
                color: #C9CCD0;
                }
                .up-info{
                        transition-delay: 0.3s;  
                        width: 123px;
                        height: 70px;
                        z-index: 10000;
                        position: absolute;
                        left: -100px;
                        bottom: -70px;
                        opacity: 0;
                        transition: opacity .3s ease,visibility .3s ease;
                        visibility: hidden;
                        display: flex;
                        flex-direction: column;
                        align-items: center;
                        justify-content: center;
                        background-color: white;
                        border: 1px solid #E3E5E7;
                        box-shadow: 0 8px 40px rgba(0, 0, 0, .1);
                        padding: 4px;
                        border-radius: 12px;

                            div{
                            width: 96%;
                            text-align: center;    
                            cursor: pointer;
                            font-size: 14px;
                            color: #61666D;
                            transition: background-color .3s ease;
                            border-radius: 8px;
                            height: 40px;
                            line-height: 40px;
                            position: relative;
                            background-color: white;
                            }
                            div:hover{
                                background-color: #F1F2F3;
                            }
                }
                .up-info2{
                        transition-delay: 0.3s;  
                        width: 123px;
                        height: 100px;
                        z-index: 10000;
                        position: absolute;
                        left: -100px;
                        bottom: -100px;
                        opacity: 0;
                        transition: opacity .3s ease,visibility .3s ease;
                        visibility: hidden;
                        display: flex;
                        flex-direction: column;
                        align-items: center;
                        justify-content: center;
                        background-color: white;
                        border: 1px solid #E3E5E7;
                        box-shadow: 0 8px 40px rgba(0, 0, 0, .1);
                        padding: 4px;
                        border-radius: 12px;

                            div{
                            width: 96%;
                            text-align: center;    
                            cursor: pointer;
                            font-size: 14px;
                            color: #61666D;
                            transition: background-color .3s ease;
                            border-radius: 8px;
                            height: 40px;
                            line-height: 40px;
                            position: relative;
                            background-color: white;
                            }
                            div:hover{
                                background-color: #F1F2F3;
                            }
                }
            }
            .up-btn:hover{
            background-color: #F1F2F3;
              
                .up-info{
                    transition-delay: 0.3s;
                    opacity: 1;
                    visibility: visible;
                }
                .up-info2{
                    transition-delay: 0.3s;
                    opacity: 1;
                    visibility: visible;
                }
            }
        }  
}

.delete-dialog-title{
    position: relative;
    text-align: center;
    font-size: 15.5px;
    font-weight: 550;
    color: #18191C;
  }
.delete-dialog-content{
    position: relative;
    text-align: center;
    top: 10px;
    font-size: 13.5px;
    color: #666;
    transition: all 0.2s ease;
}

.delete-dialog-btn-container{
    position: relative;
    top: 35px;
    display: flex;
    justify-content: center;
}

.delete-dialog-confirm-btn{
    border: 1px solid #00AEEC;
    color: white;
    border-radius: 4px;
    min-width: 130px;
    height: 32px;
    cursor: pointer;
    font-size: 15.5px;
    margin: 0 6px;
    background-color: #00AEEC;
    transition: all 0.3s ease;
}

.delete-dialog-confirm-btn:hover{
    background-color: #40C5F1;
    border: 1px solid #40C5F1;
}

.delete-dialog-cancel-btn{
    border: 1px solid #E3E5E7;
    color: #18191C;
    border-radius: 4px;
    min-width: 130px;
    height: 32px;
    cursor: pointer;
    font-size: 15.5px;
    margin: 0 6px;
    background-color: white;
    transition: all 0.3s ease;
}

.delete-dialog-cancel-btn:hover{
    background-color: #E3E5E7;
}

.delete-dialog-confirm-btn2{
    border: 1px solid #F85A54;
    color: white;
    border-radius: 4px;
    min-width: 130px;
    height: 32px;
    cursor: pointer;
    font-size: 15.5px;
    margin: 0 6px;
    background-color: #F85A54;
    transition: all 0.3s ease;
}
</style>