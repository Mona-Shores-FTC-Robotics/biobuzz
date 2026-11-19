{
  "version": "2.0.0",
  "timestamp": "2026-11-19T21:54:49.282Z",
  "startPoint": {
    "x": 62.3,
    "y": 133.8,
    "locked": false,
    "headingDeg": 270
  },
  "lines": [
    {
      "id": "l3-open",
      "color": "#4e79a7",
      "name": "to LEFT_AUTO_HIVE",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 61.4,
        "y": 120.45
      },
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 270,
        "endDeg": 268.9
      }
    },
    {
      "id": "l3-hive-park",
      "color": "#4e79a7",
      "name": "LEFT_AUTO_HIVE to LEFT_PARK (guard)",
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
          "x": 50,
          "y": 122
        },
        {
          "x": 30,
          "y": 121
        }
      ],
      "heading": {
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0,
              "endProgress": 1,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 268.9,
                "endDeg": 360
              }
            }
          ]
        }
      }
    },
    {
      "id": "link-1",
      "name": "Link (never driven)",
      "color": "#6b7280",
      "kind": "atomic",
      "endPoint": {
        "x": 61.4,
        "y": 120.45
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 268.9
      }
    },
    {
      "id": "l3-sf",
      "color": "#4e79a7",
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
        "startDeg": 268.9,
        "endDeg": 90
      }
    },
    {
      "id": "l3-sf-park",
      "color": "#4e79a7",
      "name": "SCORING FLOWER to LEFT_PARK (guard)",
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
          "x": 36,
          "y": 121
        },
        {
          "x": 24,
          "y": 119
        }
      ],
      "heading": {
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0,
              "endProgress": 1,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 90,
                "endDeg": 0
              }
            }
          ]
        }
      }
    },
    {
      "id": "link-2",
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
      "id": "l3-cross",
      "color": "#4e79a7",
      "name": "SCORING FLOWER under the HIVE to RIGHT_AUTO_HIVE",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 59.2,
        "y": 20.9
      },
      "controlPoints": [
        {
          "x": 58,
          "y": 114
        },
        {
          "x": 61,
          "y": 100
        },
        {
          "x": 61,
          "y": 70
        },
        {
          "x": 56,
          "y": 40
        }
      ],
      "heading": {
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0,
              "endProgress": 0.12,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 90,
                "endDeg": 270
              }
            },
            {
              "startProgress": 0.12,
              "endProgress": 0.82,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 270
              }
            },
            {
              "startProgress": 0.82,
              "endProgress": 0.97,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 270,
                "endDeg": 96.1
              }
            },
            {
              "startProgress": 0.97,
              "endProgress": 1,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 96.1
              }
            }
          ]
        }
      }
    },
    {
      "id": "l3-park",
      "color": "#4e79a7",
      "name": "RIGHT_AUTO_HIVE up the corridor to LEFT_PARK",
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
          "x": 61,
          "y": 30
        },
        {
          "x": 61,
          "y": 30
        },
        {
          "x": 61,
          "y": 30
        },
        {
          "x": 61,
          "y": 55
        },
        {
          "x": 61,
          "y": 55
        },
        {
          "x": 61,
          "y": 55
        },
        {
          "x": 61,
          "y": 80
        },
        {
          "x": 61,
          "y": 80
        },
        {
          "x": 61,
          "y": 80
        },
        {
          "x": 61,
          "y": 100
        },
        {
          "x": 61,
          "y": 100
        },
        {
          "x": 61,
          "y": 100
        },
        {
          "x": 57,
          "y": 112
        },
        {
          "x": 42,
          "y": 119
        },
        {
          "x": 26,
          "y": 119
        }
      ],
      "heading": {
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0,
              "endProgress": 0.05,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 96.1,
                "endDeg": 90
              }
            },
            {
              "startProgress": 0.05,
              "endProgress": 0.68,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 90
              }
            },
            {
              "startProgress": 0.68,
              "endProgress": 0.85,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 90,
                "endDeg": 0
              }
            },
            {
              "startProgress": 0.85,
              "endProgress": 1,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 0
              }
            }
          ]
        }
      }
    },
    {
      "id": "link-3",
      "name": "Link (never driven)",
      "color": "#6b7280",
      "kind": "atomic",
      "endPoint": {
        "x": 61.4,
        "y": 120.45
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 268.9
      }
    },
    {
      "id": "l3-rescue",
      "color": "#4e79a7",
      "name": "no TIP 1: under the HIVE to RIGHT_AUTO_FIX_TIP",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 55.3,
        "y": 25.7
      },
      "controlPoints": [
        {
          "x": 59,
          "y": 101.6
        },
        {
          "x": 65,
          "y": 72
        },
        {
          "x": 58.5,
          "y": 38
        }
      ],
      "heading": {
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0,
              "endProgress": 0.79,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 270
              }
            },
            {
              "startProgress": 0.79,
              "endProgress": 0.95,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 270,
                "endDeg": 90
              }
            },
            {
              "startProgress": 0.95,
              "endProgress": 1,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 90
              }
            }
          ]
        }
      }
    },
    {
      "id": "l3-rescue-park",
      "color": "#4e79a7",
      "name": "RIGHT_AUTO_FIX_TIP up the corridor to LEFT_PARK",
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
          "x": 60,
          "y": 35
        },
        {
          "x": 61,
          "y": 55
        },
        {
          "x": 61,
          "y": 55
        },
        {
          "x": 61,
          "y": 80
        },
        {
          "x": 61,
          "y": 80
        },
        {
          "x": 61,
          "y": 100
        },
        {
          "x": 61,
          "y": 100
        },
        {
          "x": 57,
          "y": 112
        },
        {
          "x": 42,
          "y": 119
        },
        {
          "x": 26,
          "y": 119
        }
      ],
      "heading": {
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0,
              "endProgress": 0.6,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 90
              }
            },
            {
              "startProgress": 0.6,
              "endProgress": 0.85,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 90,
                "endDeg": 0
              }
            },
            {
              "startProgress": 0.85,
              "endProgress": 1,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 0
              }
            }
          ]
        }
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
      "lineId": "l3-open"
    },
    {
      "kind": "path",
      "lineId": "l3-hive-park"
    },
    {
      "kind": "path",
      "lineId": "link-1"
    },
    {
      "kind": "path",
      "lineId": "l3-sf"
    },
    {
      "kind": "path",
      "lineId": "l3-sf-park"
    },
    {
      "kind": "path",
      "lineId": "link-2"
    },
    {
      "kind": "path",
      "lineId": "l3-cross"
    },
    {
      "kind": "path",
      "lineId": "l3-park"
    },
    {
      "kind": "path",
      "lineId": "link-3"
    },
    {
      "kind": "path",
      "lineId": "l3-rescue"
    },
    {
      "kind": "path",
      "lineId": "l3-rescue-park"
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
        "CellUp",
        "Tip",
        "CameraBlind",
        "Always"
      ],
      "events": [
        "Tip"
      ],
      "typicalS": {
        "LaunchAll": 2.5,
        "PickFlower": 4,
        "GardenGather": 3,
        "LaunchOne": 0.5,
        "SpinUp": 0.1
      }
    },
    "points": {
      "LEFT_AUTO_HIVE": [
        61.4,
        120.45,
        268.9
      ],
      "LEFT_PARK": [
        14.5,
        117.4,
        0
      ],
      "LEFT_START": [
        62.3,
        133.8,
        270
      ],
      "RIGHT_AUTO_FIX_TIP": [
        55.3,
        25.7,
        90
      ],
      "RIGHT_AUTO_HIVE": [
        59.2,
        20.9,
        96.1
      ],
      "SCORING_FLOWER_STAGING": [
        47.2,
        119.3,
        90
      ]
    },
    "pathEnds": {
      "l3-open": "LEFT_AUTO_HIVE",
      "l3-sf": "SCORING_FLOWER_STAGING",
      "l3-cross": "RIGHT_AUTO_HIVE",
      "l3-park": "LEFT_PARK",
      "l3-rescue": "RIGHT_AUTO_FIX_TIP",
      "l3-rescue-park": "LEFT_PARK",
      "l3-hive-park": "LEFT_PARK",
      "l3-sf-park": "LEFT_PARK"
    },
    "cards": [
      {
        "id": "c-l3-spin",
        "kind": "action",
        "name": "SpinUp"
      },
      {
        "id": "c-l3-open",
        "kind": "path",
        "lineId": "l3-open",
        "park": false
      },
      {
        "id": "c-l3-wait",
        "kind": "firstOf",
        "label": "L1: wait for TIP 1 until 11 s, otherwise rescue it",
        "rows": [
          {
            "when": [
              "LeftCellUp"
            ],
            "cards": [
              {
                "id": "c-l3-pre",
                "kind": "action",
                "name": "LaunchAll",
                "timeoutS": 1
              },
              {
                "id": "c-l3-sf",
                "kind": "path",
                "lineId": "l3-sf",
                "park": false
              },
              {
                "id": "c-l3-pick",
                "kind": "action",
                "name": "PickFlower",
                "timeoutS": 4.5
              },
              {
                "id": "c-l3-cross",
                "kind": "path",
                "lineId": "l3-cross",
                "park": false
              },
              {
                "id": "c-l3-right-check",
                "kind": "firstOf",
                "label": "L2: RIGHT up (TIP 2) by 22.5 s?",
                "rows": [
                  {
                    "when": [
                      "RightCellUp"
                    ],
                    "cards": [
                      {
                        "id": "c-l3-right",
                        "kind": "action",
                        "name": "LaunchAll",
                        "timeoutS": 1
                      }
                    ]
                  },
                  {
                    "afterMs": 22500,
                    "matchClock": true,
                    "cards": []
                  }
                ],
                "alongside": "SpinUp"
              },
              {
                "id": "c-l3-park",
                "kind": "path",
                "lineId": "l3-park",
                "park": true
              }
            ]
          },
          {
            "afterMs": 11000,
            "matchClock": true,
            "adjustable": true,
            "tip": 1,
            "cards": [
              {
                "id": "c-l3-rescue",
                "kind": "path",
                "lineId": "l3-rescue",
                "park": false
              },
              {
                "id": "c-l3-launch-rescue",
                "kind": "action",
                "name": "LaunchAll",
                "timeoutS": 1
              },
              {
                "id": "c-l3-hold",
                "kind": "firstOf",
                "label": "R has left the left end: park at 22 s",
                "rows": [
                  {
                    "afterMs": 22000,
                    "matchClock": true,
                    "cards": []
                  }
                ]
              },
              {
                "id": "c-l3-rescue-park",
                "kind": "path",
                "lineId": "l3-rescue-park",
                "park": true
              }
            ]
          }
        ],
        "alongside": "SpinUp"
      }
    ],
    "startAt": "LEFT_START",
    "linkPaths": [
      "link-1",
      "link-2",
      "link-3"
    ],
    "parkFrom": {
      "c-l3-open": "l3-hive-park",
      "c-l3-sf": "l3-sf-park",
      "c-l3-cross": "l3-park",
      "c-l3-rescue": "l3-rescue-park"
    },
    "teamSpots": [
      "LEFT_AUTO_HIVE",
      "LEFT_PARK",
      "LEFT_START",
      "RIGHT_AUTO_FIX_TIP",
      "RIGHT_AUTO_HIVE",
      "SCORING_FLOWER_STAGING"
    ],
    "exportName": "L-ThreeTip"
  }
}