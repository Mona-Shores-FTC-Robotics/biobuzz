{
  "version": "2.0.0",
  "timestamp": "2026-11-06T01:50:01.571Z",
  "startPoint": {
    "x": 55.6,
    "y": 7.7,
    "locked": false,
    "headingDeg": 90
  },
  "lines": [
    {
      "id": "line-mw0be8id-zhk73u",
      "color": "#59a14f",
      "name": "to WALL_FLOWER_STAGING",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 23.1,
        "y": 47.2
      },
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 90,
        "endDeg": 180
      }
    },
    {
      "id": "line-mw0cnl7i-qkksz9",
      "color": "#f28e2b",
      "name": "WALL_FLOWER_STAGING park",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 14.5,
        "y": 117.4
      },
      "controlPoints": [
        {
          "x": 37,
          "y": 54
        },
        {
          "x": 31,
          "y": 109
        }
      ],
      "heading": {
        "type": "linear",
        "startDeg": 180,
        "endDeg": 0
      }
    },
    {
      "id": "link-1",
      "name": "Link (never driven)",
      "color": "#6b7280",
      "kind": "atomic",
      "endPoint": {
        "x": 23.1,
        "y": 47.2
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 180
      }
    },
    {
      "kind": "atomic",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "id": "nudge-approach",
      "name": "to PARTNER_CONTACT",
      "endPoint": {
        "x": 10.5,
        "y": 67.8
      },
      "controlPoints": [
        {
          "x": 17,
          "y": 47.2
        },
        {
          "x": 10.5,
          "y": 56
        }
      ],
      "heading": {
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0,
              "endProgress": 0.6,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 180,
                "endDeg": 270
              }
            },
            {
              "startProgress": 0.6,
              "endProgress": 1,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 270
              }
            }
          ]
        }
      },
      "color": "#b07aa1"
    },
    {
      "kind": "atomic",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "id": "nudge-push",
      "name": "push to PARTNER_PUSH_END",
      "endPoint": {
        "x": 10.5,
        "y": 72
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 270
      },
      "color": "#ff9da7"
    },
    {
      "kind": "atomic",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "id": "nudge-launch",
      "name": "to LEFT_LAUNCH_CLOSE",
      "endPoint": {
        "x": 59.9,
        "y": 117.5
      },
      "controlPoints": [
        {
          "x": 30,
          "y": 68
        },
        {
          "x": 36,
          "y": 68
        },
        {
          "x": 26,
          "y": 116
        }
      ],
      "heading": {
        "type": "constant",
        "degrees": 270
      },
      "color": "#4e79a7"
    },
    {
      "id": "line-mw2z05th-whpzzi",
      "color": "#edc948",
      "name": "LEFT_LAUNCH_CLOSE park",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 14.5,
        "y": 117.4
      },
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 270,
        "endDeg": 0
      }
    },
    {
      "id": "link-2",
      "name": "Link (never driven)",
      "color": "#6b7280",
      "kind": "atomic",
      "endPoint": {
        "x": 59.9,
        "y": 117.5
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": -90
      }
    },
    {
      "id": "line-mw2h249a-6eklj4",
      "color": "#59a14f",
      "name": "to SCORING_FLOWER_STAGING",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 47.2,
        "y": 119.3
      },
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 270,
        "endDeg": 90
      }
    },
    {
      "id": "line-mw2h249i-g9eamy",
      "color": "#f28e2b",
      "name": "SCORING_FLOWER_STAGING park",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 14.5,
        "y": 117.4
      },
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 90,
        "endDeg": 0
      }
    },
    {
      "id": "link-3",
      "name": "Link (never driven)",
      "color": "#6b7280",
      "kind": "atomic",
      "endPoint": {
        "x": 47.2,
        "y": 119.3
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 90
      }
    },
    {
      "id": "line-mw2z8m8r-d4cyle",
      "color": "#e15759",
      "name": "to LEFT_LAUNCH_CLOSE",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 59.9,
        "y": 117.5
      },
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 90,
        "endDeg": 270
      }
    },
    {
      "id": "line-mw2h249a-0a0w4q",
      "color": "#b07aa1",
      "name": "to LEFT_PARK",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 14.5,
        "y": 117.4
      },
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 270,
        "endDeg": 0
      }
    },
    {
      "id": "link-4",
      "name": "Link (never driven)",
      "color": "#6b7280",
      "kind": "atomic",
      "endPoint": {
        "x": 59.9,
        "y": 117.5
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": -90
      }
    },
    {
      "id": "line-mw2z1rrz-ltmk3y",
      "color": "#9c755f",
      "name": "to RIGHT_LAUNCH_CLOSE",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 56.1,
        "y": 26
      },
      "controlPoints": [
        {
          "x": 58.76832619047272,
          "y": 101.56857289931824
        },
        {
          "x": 65.20046354104544,
          "y": 71.90515781237575
        },
        {
          "x": 60.600162310030015,
          "y": 40.90098343477904
        }
      ],
      "heading": {
        "type": "piecewise",
        "startDeg": 270,
        "endDeg": 90,
        "degrees": 0,
        "reverse": false,
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0,
              "endProgress": 0.8,
              "interpolationType": "linear",
              "reversed": false,
              "continueFromPrevious": false,
              "parameters": {
                "startDeg": 270,
                "endDeg": 270
              }
            },
            {
              "startProgress": 0.8,
              "endProgress": 1,
              "interpolationType": "linear",
              "reversed": false,
              "continueFromPrevious": false,
              "parameters": {
                "startDeg": 270,
                "endDeg": 90
              }
            }
          ]
        }
      }
    },
    {
      "id": "line-mw2h249b-qfhry3",
      "color": "#4e79a7",
      "name": "to LEFT_PARK",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 14.5,
        "y": 117.4
      },
      "controlPoints": [
        {
          "x": 32,
          "y": 35
        },
        {
          "x": 33,
          "y": 111
        }
      ],
      "heading": {
        "type": "linear",
        "startDeg": 90,
        "endDeg": 0
      }
    }
  ],
  "shapes": [
    {
      "id": "biobuzz-red-hive",
      "name": "Red HIVE",
      "vertices": [
        {
          "x": 46,
          "y": 50
        },
        {
          "x": 48.6,
          "y": 50
        },
        {
          "x": 48.6,
          "y": 91.4
        },
        {
          "x": 46,
          "y": 91.4
        }
      ],
      "color": "#dc2626",
      "fillColor": "#ff6b6b"
    },
    {
      "id": "biobuzz-blue-hive",
      "name": "Blue HIVE",
      "vertices": [
        {
          "x": 96.5,
          "y": 91.4
        },
        {
          "x": 93.9,
          "y": 91.4
        },
        {
          "x": 93.9,
          "y": 50
        },
        {
          "x": 96.5,
          "y": 50
        }
      ],
      "color": "#2563eb",
      "fillColor": "#60a5fa"
    },
    {
      "id": "biobuzz-flower-top",
      "name": "Scoring-Side FLOWER",
      "vertices": [
        {
          "x": 43.4,
          "y": 141.5
        },
        {
          "x": 50.1,
          "y": 141.5
        },
        {
          "x": 50.1,
          "y": 139.4
        },
        {
          "x": 46.75,
          "y": 137.7
        },
        {
          "x": 43.4,
          "y": 139.4
        }
      ],
      "color": "#dc2626",
      "fillColor": "#ff6b6b"
    },
    {
      "id": "biobuzz-flower-right",
      "name": "Blue Alliance FLOWER",
      "vertices": [
        {
          "x": 141.5,
          "y": 98.1
        },
        {
          "x": 139.4,
          "y": 98.1
        },
        {
          "x": 137.7,
          "y": 94.75
        },
        {
          "x": 139.4,
          "y": 91.4
        },
        {
          "x": 141.5,
          "y": 91.4
        }
      ],
      "color": "#2563eb",
      "fillColor": "#60a5fa"
    },
    {
      "id": "biobuzz-flower-bottom",
      "name": "Audience-Side FLOWER",
      "vertices": [
        {
          "x": 91.15,
          "y": 0
        },
        {
          "x": 97.8,
          "y": 0
        },
        {
          "x": 97.8,
          "y": 2.1
        },
        {
          "x": 94.5,
          "y": 3.8
        },
        {
          "x": 91.15,
          "y": 2.1
        }
      ],
      "color": "#2563eb",
      "fillColor": "#60a5fa"
    },
    {
      "id": "biobuzz-flower-left",
      "name": "Red Alliance FLOWER",
      "vertices": [
        {
          "x": 0,
          "y": 50.35
        },
        {
          "x": 2.1,
          "y": 50.35
        },
        {
          "x": 3.8,
          "y": 47
        },
        {
          "x": 2.1,
          "y": 43.65
        },
        {
          "x": 0,
          "y": 43.65
        }
      ],
      "color": "#dc2626",
      "fillColor": "#ff6b6b"
    }
  ],
  "sequence": [
    {
      "kind": "path",
      "lineId": "line-mw0be8id-zhk73u"
    },
    {
      "kind": "path",
      "lineId": "line-mw0cnl7i-qkksz9"
    },
    {
      "kind": "path",
      "lineId": "link-1"
    },
    {
      "kind": "path",
      "lineId": "nudge-approach"
    },
    {
      "kind": "path",
      "lineId": "nudge-push"
    },
    {
      "kind": "path",
      "lineId": "nudge-launch"
    },
    {
      "kind": "path",
      "lineId": "line-mw2z05th-whpzzi"
    },
    {
      "kind": "path",
      "lineId": "link-2"
    },
    {
      "kind": "path",
      "lineId": "line-mw2h249a-6eklj4"
    },
    {
      "kind": "path",
      "lineId": "line-mw2h249i-g9eamy"
    },
    {
      "kind": "path",
      "lineId": "link-3"
    },
    {
      "kind": "path",
      "lineId": "line-mw2z8m8r-d4cyle"
    },
    {
      "kind": "path",
      "lineId": "line-mw2h249a-0a0w4q"
    },
    {
      "kind": "path",
      "lineId": "link-4"
    },
    {
      "kind": "path",
      "lineId": "line-mw2z1rrz-ltmk3y"
    },
    {
      "kind": "path",
      "lineId": "line-mw2h249b-qfhry3"
    }
  ],
  "fieldPoints": [],
  "robot": {
    "forwardVelocity": 75,
    "strafeVelocity": 65,
    "aVelocity": 3.141592653589793,
    "kFriction": 0.1,
    "rLength": 15.44,
    "rWidth": 16,
    "rForward": 0,
    "safetyMargin": 1,
    "maxVelocity": 50,
    "maxAcceleration": 50,
    "maxDeceleration": 50,
    "features": [
      {
        "name": "FLOWER poker",
        "length": 1.5,
        "width": 1.5,
        "forward": 8.5,
        "left": 0,
        "collides": true
      }
    ]
  },
  "activePaths": [],
  "auto": {
    "version": 1,
    "drawnFor": "RED",
    "registry": {
      "actions": [
        "LaunchAll",
        "PickFlower",
        "GardenGather",
        "LaunchOne",
        "SpinUp"
      ],
      "conditions": [
        "Empty",
        "IntakeFull",
        "LeftCellUp",
        "RightCellUp",
        "Tip",
        "CameraBlind",
        "CellUp"
      ],
      "events": [
        "Tip"
      ],
      "typicalS": {
        "LaunchAll": 4.5,
        "PickFlower": 3.7,
        "GardenGather": 5,
        "LaunchOne": 0.5,
        "SpinUp": 0.1
      }
    },
    "points": {
      "RIGHT_START": [
        55.6,
        7.7,
        90
      ],
      "WALL_FLOWER_STAGING": [
        23.1,
        47.2,
        180
      ],
      "SCORING_FLOWER_STAGING": [
        47.2,
        119.3,
        90
      ],
      "LEFT_LAUNCH_CLOSE": [
        59.9,
        117.5,
        270
      ],
      "RIGHT_LAUNCH_CLOSE": [
        56.1,
        26,
        90
      ],
      "LEFT_PARK": [
        14.5,
        117.4,
        0
      ],
      "PARTNER_CONTACT": [
        10.5,
        67.8,
        270
      ],
      "PARTNER_PUSH_END": [
        10.5,
        72,
        270
      ]
    },
    "pathEnds": {
      "line-mw0be8id-zhk73u": "WALL_FLOWER_STAGING",
      "line-mw0cnl7i-qkksz9": "LEFT_PARK",
      "line-mw2h249a-6eklj4": "SCORING_FLOWER_STAGING",
      "line-mw2h249a-0a0w4q": "LEFT_PARK",
      "line-mw2h249b-qfhry3": "LEFT_PARK",
      "line-mw2h249i-g9eamy": "LEFT_PARK",
      "line-mw2z05th-whpzzi": "LEFT_PARK",
      "line-mw2z1rrz-ltmk3y": "RIGHT_LAUNCH_CLOSE",
      "line-mw2z8m8r-d4cyle": "LEFT_LAUNCH_CLOSE",
      "nudge-approach": "PARTNER_CONTACT",
      "nudge-push": "PARTNER_PUSH_END",
      "nudge-launch": "LEFT_LAUNCH_CLOSE"
    },
    "cards": [
      {
        "id": "c-mw0be8ic-8rwew",
        "kind": "action",
        "name": "SpinUp"
      },
      {
        "id": "c-mw0be8ic-jdurq",
        "kind": "action",
        "name": "LaunchAll"
      },
      {
        "id": "c-mw0be8id-e596r",
        "kind": "path",
        "lineId": "line-mw0be8id-zhk73u",
        "park": false
      },
      {
        "id": "c-mw0be8id-fud0d",
        "kind": "action",
        "name": "PickFlower",
        "timeoutS": 4.5
      },
      {
        "id": "nudge-approach-card",
        "kind": "path",
        "lineId": "nudge-approach",
        "park": false
      },
      {
        "id": "nudge-contact-pause",
        "kind": "firstOf",
        "label": "Settle before contact",
        "rows": [
          {
            "afterMs": 300,
            "cards": []
          }
        ]
      },
      {
        "id": "nudge-push-card",
        "kind": "path",
        "lineId": "nudge-push",
        "park": false
      },
      {
        "id": "nudge-launch-card",
        "kind": "path",
        "lineId": "nudge-launch",
        "park": false
      },
      {
        "id": "c-mw2h249a-c0rs6",
        "kind": "firstOf",
        "label": "Wait for TIP or CELL up",
        "rows": [
          {
            "when": [
              "CellUp"
            ],
            "cards": [
              {
                "id": "c-mw2h249a-0np9s",
                "kind": "action",
                "name": "LaunchAll"
              },
              {
                "id": "c-mw2h249a-bml5p",
                "kind": "path",
                "lineId": "line-mw2h249a-6eklj4",
                "park": false
              },
              {
                "id": "c-mw2h249a-p7er4",
                "kind": "action",
                "name": "PickFlower",
                "timeoutS": 4.5
              },
              {
                "id": "c-mw2z8m8r-obclc",
                "kind": "path",
                "lineId": "line-mw2z8m8r-d4cyle",
                "park": false
              },
              {
                "id": "c-mw2h249a-p5yh3",
                "kind": "action",
                "name": "LaunchAll"
              },
              {
                "id": "c-mw2h249a-nxkzp",
                "kind": "path",
                "lineId": "line-mw2h249a-0a0w4q",
                "park": true
              }
            ]
          },
          {
            "afterMs": 750,
            "cards": [
              {
                "id": "c-mw2z1rrz-w27kx",
                "kind": "path",
                "lineId": "line-mw2z1rrz-ltmk3y",
                "park": false
              },
              {
                "id": "c-mw2h249b-xpbq7",
                "kind": "action",
                "name": "LaunchAll"
              },
              {
                "id": "c-mw2h249b-tphk6",
                "kind": "path",
                "lineId": "line-mw2h249b-qfhry3",
                "park": true
              }
            ]
          }
        ]
      }
    ],
    "startAt": "RIGHT_START",
    "linkPaths": [
      "link-1",
      "link-2",
      "link-3",
      "link-4"
    ],
    "parkFrom": {
      "c-mw0be8id-e596r": "line-mw0cnl7i-qkksz9",
      "c-mw2h249a-bml5p": "line-mw2h249i-g9eamy",
      "nudge-launch-card": "line-mw2z05th-whpzzi"
    },
    "teamSpots": [
      "LEFT_LAUNCH_CLOSE",
      "RIGHT_LAUNCH_CLOSE",
      "LEFT_PARK",
      "RIGHT_START",
      "SCORING_FLOWER_STAGING",
      "WALL_FLOWER_STAGING"
    ],
    "exportName": "R-Solo-Nudge"
  }
}
