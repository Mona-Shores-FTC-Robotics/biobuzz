{
  "version": "2.0.0",
  "timestamp": "2026-11-19T18:00:00.000Z",
  "startPoint": {
    "x": 55.6,
    "y": 7.7,
    "locked": false,
    "headingDeg": 90
  },
  "lines": [
    {
      "id": "r3-garden",
      "color": "#4e79a7",
      "name": "to GARDEN_STAGING",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 11.5,
        "y": 22.1
      },
      "controlPoints": [],
      "heading": {
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0,
              "endProgress": 0.5,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 90,
                "endDeg": 180
              }
            },
            {
              "startProgress": 0.5,
              "endProgress": 1,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 180,
                "endDeg": 270
              }
            }
          ]
        }
      }
    },
    {
      "id": "r3-to-left",
      "color": "#4e79a7",
      "name": "GARDEN up the outer lane to R_LEFT_WEST",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 33,
        "y": 98
      },
      "controlPoints": [
        {
          "x": 18,
          "y": 45
        },
        {
          "x": 22,
          "y": 70
        },
        {
          "x": 26,
          "y": 90
        }
      ],
      "heading": {
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0,
              "endProgress": 0.35,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 270,
                "endDeg": 450
              }
            },
            {
              "startProgress": 0.35,
              "endProgress": 0.8,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 450
              }
            },
            {
              "startProgress": 0.8,
              "endProgress": 1,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 450,
                "endDeg": 335.8
              }
            }
          ]
        }
      }
    },
    {
      "id": "r3-left-wf",
      "color": "#4e79a7",
      "name": "R_LEFT_WEST down the outer lane to WALL_FLOWER_STAGING",
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
      "controlPoints": [
        {
          "x": 26,
          "y": 88
        },
        {
          "x": 23,
          "y": 68
        }
      ],
      "heading": {
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0,
              "endProgress": 0.3,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 335.8,
                "endDeg": 270
              }
            },
            {
              "startProgress": 0.3,
              "endProgress": 0.7,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 270
              }
            },
            {
              "startProgress": 0.7,
              "endProgress": 1,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 270,
                "endDeg": 180
              }
            }
          ]
        }
      }
    },
    {
      "id": "r3-wf-spot",
      "color": "#4e79a7",
      "name": "WALL FLOWER to R_RIGHT_WF",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 27,
        "y": 50
      },
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 180,
        "endDeg": 18.7
      }
    },
    {
      "id": "r3-park",
      "color": "#4e79a7",
      "name": "R_RIGHT_WF up the outer lane to RIGHT_PARK",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 14.5,
        "y": 98.7
      },
      "controlPoints": [
        {
          "x": 27,
          "y": 70
        },
        {
          "x": 26,
          "y": 90
        }
      ],
      "heading": {
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0,
              "endProgress": 0.2,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 18.7,
                "endDeg": 90
              }
            },
            {
              "startProgress": 0.2,
              "endProgress": 0.7,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 90
              }
            },
            {
              "startProgress": 0.7,
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
      "id": "r3-garden-park",
      "color": "#4e79a7",
      "name": "GARDEN up the outer lane to RIGHT_PARK (guard)",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 14.5,
        "y": 98.7
      },
      "controlPoints": [
        {
          "x": 18,
          "y": 45
        },
        {
          "x": 24,
          "y": 75
        }
      ],
      "heading": {
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0,
              "endProgress": 0.15,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 270,
                "endDeg": 450
              }
            },
            {
              "startProgress": 0.15,
              "endProgress": 0.8,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 450
              }
            },
            {
              "startProgress": 0.8,
              "endProgress": 1,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 450,
                "endDeg": 360
              }
            }
          ]
        }
      }
    },
    {
      "id": "r3-left-park",
      "color": "#4e79a7",
      "name": "R_LEFT_WEST to RIGHT_PARK",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 14.5,
        "y": 98.7
      },
      "controlPoints": [
        {
          "x": 24,
          "y": 98
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
                "startDeg": 335.8,
                "endDeg": 360
              }
            }
          ]
        }
      }
    },
    {
      "id": "r3-wf-park",
      "color": "#4e79a7",
      "name": "WALL FLOWER to RIGHT_PARK (guard)",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 14.5,
        "y": 98.7
      },
      "controlPoints": [
        {
          "x": 26,
          "y": 75
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
                "startDeg": 180,
                "endDeg": 0
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
      "lineId": "r3-garden"
    },
    {
      "kind": "path",
      "lineId": "r3-to-left"
    },
    {
      "kind": "path",
      "lineId": "r3-left-wf"
    },
    {
      "kind": "path",
      "lineId": "r3-wf-spot"
    },
    {
      "kind": "path",
      "lineId": "r3-park"
    },
    {
      "kind": "path",
      "lineId": "r3-garden-park"
    },
    {
      "kind": "path",
      "lineId": "r3-left-park"
    },
    {
      "kind": "path",
      "lineId": "r3-wf-park"
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
        "LaunchAll": 4.5,
        "PickFlower": 4,
        "GardenGather": 5,
        "LaunchOne": 0.5,
        "SpinUp": 0.1
      }
    },
    "points": {
      "GARDEN_STAGING": [
        11.5,
        22.1,
        270
      ],
      "RIGHT_PARK": [
        14.5,
        98.7,
        0
      ],
      "RIGHT_START": [
        55.6,
        7.7,
        90
      ],
      "R_LEFT_WEST": [
        33,
        98,
        335.8
      ],
      "R_RIGHT_WF": [
        27,
        50,
        18.7
      ],
      "WALL_FLOWER_STAGING": [
        23.1,
        47.2,
        180
      ]
    },
    "pathEnds": {
      "r3-garden": "GARDEN_STAGING",
      "r3-to-left": "R_LEFT_WEST",
      "r3-left-wf": "WALL_FLOWER_STAGING",
      "r3-wf-spot": "R_RIGHT_WF",
      "r3-park": "RIGHT_PARK",
      "r3-garden-park": "RIGHT_PARK",
      "r3-left-park": "RIGHT_PARK",
      "r3-wf-park": "RIGHT_PARK"
    },
    "cards": [
      {
        "id": "c-r3-spin",
        "kind": "action",
        "name": "SpinUp"
      },
      {
        "id": "c-r3-pre",
        "kind": "action",
        "name": "LaunchAll",
        "timeoutS": 2.5
      },
      {
        "id": "c-r3-garden",
        "kind": "path",
        "lineId": "r3-garden",
        "park": false
      },
      {
        "id": "c-r3-gather",
        "kind": "action",
        "name": "GardenGather",
        "timeoutS": 5.5
      },
      {
        "id": "c-r3-to-left",
        "kind": "path",
        "lineId": "r3-to-left",
        "park": false
      },
      {
        "id": "c-r3-left-wait",
        "kind": "firstOf",
        "label": "R2: LEFT up (TIP 1) by 14 s, otherwise hold for L's rescue",
        "alongside": "SpinUp",
        "rows": [
          {
            "when": [
              "LeftCellUp"
            ],
            "cards": [
              {
                "id": "c-r3-left",
                "kind": "action",
                "name": "LaunchAll",
                "timeoutS": 1
              },
              {
                "id": "c-r3-wf",
                "kind": "path",
                "lineId": "r3-left-wf",
                "park": false
              },
              {
                "id": "c-r3-pick",
                "kind": "action",
                "name": "PickFlower",
                "timeoutS": 4.5
              },
              {
                "id": "c-r3-offset",
                "kind": "path",
                "lineId": "r3-wf-spot",
                "park": false
              },
              {
                "id": "c-r3-right-check",
                "kind": "firstOf",
                "label": "R3: RIGHT up (TIP 2) by 24 s?",
                "alongside": "SpinUp",
                "rows": [
                  {
                    "when": [
                      "RightCellUp"
                    ],
                    "cards": [
                      {
                        "id": "c-r3-right",
                        "kind": "action",
                        "name": "LaunchAll",
                        "timeoutS": 1
                      }
                    ]
                  },
                  {
                    "afterMs": 24000,
                    "matchClock": true,
                    "cards": []
                  }
                ]
              },
              {
                "id": "c-r3-park",
                "kind": "path",
                "lineId": "r3-park",
                "park": true
              }
            ]
          },
          {
            "afterMs": 14000,
            "matchClock": true,
            "adjustable": true,
            "tip": 1,
            "cards": [
              {
                "id": "c-r3-late-check",
                "kind": "firstOf",
                "label": "LEFT up after L's rescue by 21 s? Fire at LEFT, else park",
                "rows": [
                  {
                    "when": [
                      "LeftCellUp"
                    ],
                    "cards": [
                      {
                        "id": "c-r3-left-late",
                        "kind": "action",
                        "name": "LaunchAll",
                        "timeoutS": 1
                      }
                    ]
                  },
                  {
                    "afterMs": 21000,
                    "matchClock": true,
                    "cards": []
                  }
                ]
              },
              {
                "id": "c-r3-left-park",
                "kind": "path",
                "lineId": "r3-left-park",
                "park": true
              }
            ]
          }
        ]
      }
    ],
    "startAt": "RIGHT_START",
    "linkPaths": [],
    "parkFrom": {
      "c-r3-garden": "r3-garden-park",
      "c-r3-to-left": "r3-left-park",
      "c-r3-wf": "r3-wf-park",
      "c-r3-offset": "r3-park"
    },
    "teamSpots": [
      "GARDEN_STAGING",
      "RIGHT_PARK",
      "RIGHT_START",
      "WALL_FLOWER_STAGING"
    ],
    "exportName": "R-ThreeTip"
  }
}
