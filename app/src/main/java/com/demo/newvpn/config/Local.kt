package com.demo.newvpn.config

object Local {
    const val URL=""
    const val EMAIL=""

    const val OPEN="securenet_start"
    const val CONNECT="securenet_inb"
    const val HOME_BOTTOM="securenet_home"
    const val RESULT="securenet_down"
    const val BACK="securenet_cba"
    const val SERVER_TOP="securenet_se"

    const val localServer="""[
    {
        "pwd":"123456",
        "encryption":"chacha20-ietf-poly1305",
        "port":100,
        "country":"Japan",
        "city":"Tokyo",
        "ip":"100.223.52.0"
    },
    {
        "pwd":"123456",
        "encryption":"chacha20-ietf-poly1305",
        "port":100,
        "country":"UnitedStates",
        "city":"NewYork",
        "ip":"100.223.52.1"
    }
]"""

    const val localAdStr2="""{
    "securenet_click":15,
    "securenet_show":50,
    "securenet_start":[
        {
            "securenet_ab":"admob",
            "securenet_ad":"ca-app-pub-3940256099942544/3419835294",
            "securenet_ac":"o",
            "securenet_ae":2
        }
    ],
    "securenet_home":[
        {
            "securenet_ab":"admob",
            "securenet_ad":"ca-app-pub-3940256099942544/2247696110",
            "securenet_ac":"n",
            "securenet_ae":2
        },
        {
            "securenet_ab":"admob",
            "securenet_ad":"ca-app-pub-3940256099942544/2247696110A",
            "securenet_ac":"n",
            "securenet_ae":3
        },
        {
            "securenet_ab":"admob",
            "securenet_ad":"ca-app-pub-3940256099942544/1044960115",
            "securenet_ac":"n",
            "securenet_ae":1
        }
    ],
    "securenet_down":[
        {
            "securenet_ab":"admob",
            "securenet_ad":"ca-app-pub-3940256099942544/2247696110",
            "securenet_ac":"n",
            "securenet_ae":1
        },
        {
            "securenet_ab":"admob",
            "securenet_ad":"ca-app-pub-3940256099942544/2247696110A",
            "securenet_ac":"n",
            "securenet_ae":3
        },
        {
            "securenet_ab":"admob",
            "securenet_ad":"ca-app-pub-3940256099942544/1044960115",
            "securenet_ac":"n",
            "securenet_ae":2
        }
    ],
    "securenet_se":[
        {
            "securenet_ab":"admob",
            "securenet_ad":"ca-app-pub-3940256099942544/2247696110",
            "securenet_ac":"n",
            "securenet_ae":1
        },
        {
            "securenet_ab":"admob",
            "securenet_ad":"ca-app-pub-3940256099942544/2247696110A",
            "securenet_ac":"n",
            "securenet_ae":3
        },
        {
            "securenet_ab":"admob",
            "securenet_ad":"ca-app-pub-3940256099942544/1044960115",
            "securenet_ac":"n",
            "securenet_ae":2
        }
    ],
    "securenet_inb":[
        {
            "securenet_ab":"admob",
            "securenet_ad":"ca-app-pub-3940256099942544/1033173712",
            "securenet_ac":"inr",
            "securenet_ae":2
        },
        {
            "securenet_ab":"admob",
            "securenet_ad":"ca-app-pub-3940256099942544/8691691433",
            "securenet_ac":"inr",
            "securenet_ae":1
        },
        {
            "securenet_ab":"admob",
            "securenet_ad":"ca-app-pub-3940256099942544/8691691433",
            "securenet_ac":"inr",
            "securenet_ae":3
        }
    ],
    "securenet_cba":[
        {
            "securenet_ab":"admob",
            "securenet_ad":"ca-app-pub-3940256099942544/1033173712",
            "securenet_ac":"inr",
            "securenet_ae":1
        },
        {
            "securenet_ab":"admob",
            "securenet_ad":"ca-app-pub-3940256099942544/8691691433",
            "securenet_ac":"inr",
            "securenet_ae":2
        },
        {
            "securenet_ab":"admob",
            "securenet_ad":"ca-app-pub-3940256099942544/8691691433XX",
            "securenet_ac":"inr",
            "securenet_ae":3
        }
    ]
}"""
}