{
  "version": "2.0.0",
  "timestamp": "2026-11-13T02:21:31.254Z",
  "startPoint": {
    "x": 62.3,
    "y": 133.8,
    "locked": false,
    "headingDeg": 270
  },
  "lines": [
    {
      "id": "l-open",
      "color": "#4e79a7",
      "name": "open to LEFT_WALL_HIVE",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 59.9,
        "y": 133.4
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 270
      }
    },
    {
      "id": "l-catch-park",
      "color": "#4e79a7",
      "name": "catch park to LEFT_PARK",
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
          "x": 59,
          "y": 121
        },
        {
          "x": 40,
          "y": 117.4
        }
      ],
      "heading": {
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0,
              "endProgress": 0.7,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 270,
                "endDeg": 360
              }
            },
            {
              "startProgress": 0.7,
              "endProgress": 1,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 360
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
        "x": 59.9,
        "y": 133.4
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 270
      }
    },
    {
      "id": "l-rescue",
      "color": "#4e79a7",
      "name": "under the HIVE to RIGHT_START",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "compound",
      "segments": [
        {
          "id": "l-rescue-south",
          "color": "#f28e2b",
          "name": "",
          "locked": false,
          "waitBeforeMs": 0,
          "waitAfterMs": 0,
          "waitBeforeName": "",
          "waitAfterName": "",
          "kind": "atomic",
          "endPoint": {
            "x": 58.3,
            "y": 42
          },
          "controlPoints": [
            {
              "x": 58.76832619047272,
              "y": 101.56857289931824
            },
            {
              "x": 65.20046354104544,
              "y": 71.90515781237575
            }
          ],
          "heading": {
            "type": "constant",
            "degrees": 270
          }
        },
        {
          "id": "l-rescue-turn",
          "color": "#edc948",
          "name": "",
          "locked": false,
          "waitBeforeMs": 0,
          "waitAfterMs": 0,
          "waitBeforeName": "",
          "waitAfterName": "",
          "kind": "atomic",
          "endPoint": {
            "x": 56.5,
            "y": 24
          },
          "controlPoints": [],
          "heading": {
            "type": "linear",
            "startDeg": 270,
            "endDeg": 90
          }
        },
        {
          "id": "l-rescue-aim",
          "color": "#59a14f",
          "name": "",
          "locked": false,
          "waitBeforeMs": 0,
          "waitAfterMs": 0,
          "waitBeforeName": "",
          "waitAfterName": "",
          "kind": "atomic",
          "endPoint": {
            "x": 55.6,
            "y": 7.7
          },
          "controlPoints": [],
          "heading": {
            "type": "constant",
            "degrees": 90
          }
        }
      ]
    },
    {
      "id": "l-rescue-park",
      "color": "#4e79a7",
      "name": "right catch park to LEFT_PARK",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "compound",
      "segments": [
        {
          "id": "l-rescue-park-clear",
          "color": "#f28e2b",
          "name": "",
          "locked": false,
          "waitBeforeMs": 0,
          "waitAfterMs": 0,
          "waitBeforeName": "",
          "waitAfterName": "",
          "kind": "atomic",
          "endPoint": {
            "x": 50,
            "y": 38
          },
          "controlPoints": [
            {
              "x": 55.6,
              "y": 28
            },
            {
              "x": 56,
              "y": 38
            }
          ],
          "heading": {
            "type": "constant",
            "degrees": 90
          }
        },
        {
          "id": "l-rescue-park-west",
          "color": "#e15759",
          "name": "",
          "locked": false,
          "waitBeforeMs": 0,
          "waitAfterMs": 0,
          "waitBeforeName": "",
          "waitAfterName": "",
          "kind": "atomic",
          "endPoint": {
            "x": 36,
            "y": 38
          },
          "controlPoints": [],
          "heading": {
            "type": "constant",
            "degrees": 90
          }
        },
        {
          "id": "l-rescue-park-lane",
          "color": "#f28e2b",
          "name": "",
          "locked": false,
          "waitBeforeMs": 0,
          "waitAfterMs": 0,
          "waitBeforeName": "",
          "waitAfterName": "",
          "kind": "atomic",
          "endPoint": {
            "x": 34,
            "y": 106
          },
          "controlPoints": [
            {
              "x": 31,
              "y": 40
            },
            {
              "x": 33,
              "y": 92
            }
          ],
          "heading": {
            "type": "constant",
            "degrees": 90
          }
        },
        {
          "id": "l-rescue-park-turn-face",
          "color": "#b07aa1",
          "name": "",
          "locked": false,
          "waitBeforeMs": 0,
          "waitAfterMs": 0,
          "waitBeforeName": "",
          "waitAfterName": "",
          "kind": "atomic",
          "endPoint": {
            "x": 36,
            "y": 118.2
          },
          "controlPoints": [
            {
              "x": 35,
              "y": 112
            }
          ],
          "heading": {
            "type": "linear",
            "startDeg": 90,
            "endDeg": 0
          }
        },
        {
          "id": "l-rescue-park-turn",
          "color": "#b07aa1",
          "name": "",
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
            "type": "constant",
            "degrees": 0
          }
        }
      ]
    },
    {
      "id": "link-2",
      "name": "Link (never driven)",
      "color": "#6b7280",
      "kind": "atomic",
      "endPoint": {
        "x": 59.9,
        "y": 133.4
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 270
      }
    },
    {
      "id": "l-pickup",
      "color": "#4e79a7",
      "name": "under the HIVE to GARDEN_STAGING",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "compound",
      "segments": [
        {
          "id": "l-under-hive",
          "color": "#f28e2b",
          "name": "",
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
            "type": "constant",
            "degrees": 270
          }
        },
        {
          "id": "l-to-garden",
          "color": "#f28e2b",
          "name": "",
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
            "type": "constant",
            "degrees": 270
          }
        }
      ]
    },
    {
      "id": "l-pickup-park",
      "color": "#4e79a7",
      "name": "pickup park to LEFT_PARK",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "compound",
      "segments": [
        {
          "id": "l-pickup-park-lane",
          "color": "#4e79a7",
          "name": "",
          "locked": false,
          "waitBeforeMs": 0,
          "waitAfterMs": 0,
          "waitBeforeName": "",
          "waitAfterName": "",
          "kind": "atomic",
          "endPoint": {
            "x": 34,
            "y": 106
          },
          "controlPoints": [
            {
              "x": 31,
              "y": 38
            },
            {
              "x": 33,
              "y": 92
            }
          ],
          "heading": {
            "type": "linear",
            "startDeg": 270,
            "endDeg": 90
          }
        },
        {
          "id": "l-pickup-park-turn-face",
          "color": "#76b7b2",
          "name": "",
          "locked": false,
          "waitBeforeMs": 0,
          "waitAfterMs": 0,
          "waitBeforeName": "",
          "waitAfterName": "",
          "kind": "atomic",
          "endPoint": {
            "x": 36,
            "y": 118.2
          },
          "controlPoints": [
            {
              "x": 35,
              "y": 112
            }
          ],
          "heading": {
            "type": "linear",
            "startDeg": 90,
            "endDeg": 0
          }
        },
        {
          "id": "l-pickup-park-turn",
          "color": "#76b7b2",
          "name": "",
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
            "type": "constant",
            "degrees": 0
          }
        }
      ]
    },
    {
      "id": "link-3",
      "name": "Link (never driven)",
      "color": "#6b7280",
      "kind": "atomic",
      "endPoint": {
        "x": 11.5,
        "y": 22.1
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 270
      }
    },
    {
      "id": "l-to-offset",
      "color": "#4e79a7",
      "name": "GARDEN to RIGHT_WALL_NEAR",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 35.9,
        "y": 10.6
      },
      "controlPoints": [
        {
          "x": 21,
          "y": 14
        }
      ],
      "heading": {
        "type": "linear",
        "startDeg": 270,
        "endDeg": 426.9
      }
    },
    {
      "id": "l-park",
      "color": "#4e79a7",
      "name": "outside lane to LEFT_PARK",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "compound",
      "segments": [
        {
          "id": "l-park-lane",
          "color": "#59a14f",
          "name": "",
          "locked": false,
          "waitBeforeMs": 0,
          "waitAfterMs": 0,
          "waitBeforeName": "",
          "waitAfterName": "",
          "kind": "atomic",
          "endPoint": {
            "x": 34,
            "y": 106
          },
          "controlPoints": [
            {
              "x": 25,
              "y": 20
            },
            {
              "x": 35,
              "y": 92
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
                    "startDeg": 66.9,
                    "endDeg": 90
                  }
                },
                {
                  "startProgress": 0.2,
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
          "id": "l-park-turn-face",
          "color": "#59a14f",
          "name": "",
          "locked": false,
          "waitBeforeMs": 0,
          "waitAfterMs": 0,
          "waitBeforeName": "",
          "waitAfterName": "",
          "kind": "atomic",
          "endPoint": {
            "x": 36,
            "y": 118.2
          },
          "controlPoints": [
            {
              "x": 35,
              "y": 112
            }
          ],
          "heading": {
            "type": "linear",
            "startDeg": 90,
            "endDeg": 0
          }
        },
        {
          "id": "l-park-turn",
          "color": "#e15759",
          "name": "",
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
            "type": "constant",
            "degrees": 0
          }
        }
      ]
    }
  ],
  "shapes": [],
  "sequence": [
    {
      "kind": "path",
      "lineId": "l-open"
    },
    {
      "kind": "path",
      "lineId": "l-catch-park"
    },
    {
      "kind": "path",
      "lineId": "link-1"
    },
    {
      "kind": "path",
      "lineId": "l-rescue-south"
    },
    {
      "kind": "path",
      "lineId": "l-rescue-turn"
    },
    {
      "kind": "path",
      "lineId": "l-rescue-aim"
    },
    {
      "kind": "path",
      "lineId": "l-rescue-park-clear"
    },
    {
      "kind": "path",
      "lineId": "l-rescue-park-west"
    },
    {
      "kind": "path",
      "lineId": "l-rescue-park-lane"
    },
    {
      "kind": "path",
      "lineId": "l-rescue-park-turn-face"
    },
    {
      "kind": "path",
      "lineId": "l-rescue-park-turn"
    },
    {
      "kind": "path",
      "lineId": "link-2"
    },
    {
      "kind": "path",
      "lineId": "l-under-hive"
    },
    {
      "kind": "path",
      "lineId": "l-to-garden"
    },
    {
      "kind": "path",
      "lineId": "l-pickup-park-lane"
    },
    {
      "kind": "path",
      "lineId": "l-pickup-park-turn-face"
    },
    {
      "kind": "path",
      "lineId": "l-pickup-park-turn"
    },
    {
      "kind": "path",
      "lineId": "link-3"
    },
    {
      "kind": "path",
      "lineId": "l-to-offset"
    },
    {
      "kind": "path",
      "lineId": "l-park-lane"
    },
    {
      "kind": "path",
      "lineId": "l-park-turn-face"
    },
    {
      "kind": "path",
      "lineId": "l-park-turn"
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
        "LaunchOne",
        "SpinUp",
        "PickFlower",
        "GardenGather"
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
        "LaunchAll": 4,
        "LaunchOne": 0.5,
        "SpinUp": 0.1,
        "PickFlower": 4,
        "GardenGather": 5
      }
    },
    "points": {
      "LEFT_START": [
        62.3,
        133.8,
        270
      ],
      "LEFT_WALL_HIVE": [
        59.9,
        133.4,
        270
      ],
      "RIGHT_START": [
        55.6,
        7.7,
        90
      ],
      "GARDEN_STAGING": [
        11.5,
        22.1,
        270
      ],
      "RIGHT_WALL_NEAR": [
        35.9,
        10.6,
        66.9
      ],
      "LEFT_PARK": [
        14.5,
        117.4,
        0
      ]
    },
    "pathEnds": {
      "l-open": "LEFT_WALL_HIVE",
      "l-catch-park": "LEFT_PARK",
      "l-rescue-aim": "RIGHT_START",
      "l-rescue-park-turn": "LEFT_PARK",
      "l-to-garden": "GARDEN_STAGING",
      "l-to-offset": "RIGHT_WALL_NEAR",
      "l-pickup-park-turn": "LEFT_PARK",
      "l-park-turn": "LEFT_PARK"
    },
    "cards": [
      {
        "id": "l-spin",
        "kind": "action",
        "name": "SpinUp"
      },
      {
        "id": "l-c-open",
        "kind": "path",
        "lineId": "l-open",
        "park": false
      },
      {
        "id": "l-stop-1",
        "kind": "firstOf",
        "label": "L1: LEFT up by 9.5 s, otherwise cross and finish TIP 1 at RIGHT",
        "rows": [
          {
            "afterMs": 9500,
            "matchClock": true,
            "cards": [
              {
                "id": "l-c-rescue",
                "kind": "path",
                "lineId": "l-rescue",
                "park": false
              },
              {
                "id": "l-launch-rescue",
                "kind": "action",
                "name": "LaunchAll",
                "timeoutS": 1
              },
              {
                "id": "l-hold-rescue",
                "kind": "firstOf",
                "label": "R has parked from the left spot: park at 22.5 s",
                "rows": [
                  {
                    "afterMs": 22500,
                    "matchClock": true,
                    "cards": []
                  }
                ]
              },
              {
                "id": "l-c-rescue-park",
                "kind": "path",
                "lineId": "l-rescue-park",
                "park": true
              }
            ]
          },
          {
            "when": [
              "LeftCellUp"
            ],
            "cards": [
              {
                "id": "l-launch-left",
                "kind": "action",
                "name": "LaunchAll",
                "timeoutS": 1
              },
              {
                "id": "l-c-pickup",
                "kind": "path",
                "lineId": "l-pickup",
                "park": false
              },
              {
                "id": "l-grab",
                "kind": "action",
                "name": "GardenGather",
                "timeoutS": 5.5
              },
              {
                "id": "l-c-offset",
                "kind": "path",
                "lineId": "l-to-offset",
                "park": false
              },
              {
                "id": "l-stop-2",
                "kind": "firstOf",
                "label": "L2: RIGHT up by 19.2 s, otherwise keep the load",
                "rows": [
                  {
                    "afterMs": 19200,
                    "matchClock": true,
                    "cards": [
                      {
                        "id": "l-hold-no",
                        "kind": "firstOf",
                        "label": "R has left the scoring FLOWER: park at 22.7 s",
                        "rows": [
                          {
                            "afterMs": 22700,
                            "matchClock": true,
                            "cards": []
                          }
                        ]
                      },
                      {
                        "id": "l-c-park-no",
                        "kind": "path",
                        "lineId": "l-park",
                        "park": true
                      }
                    ]
                  },
                  {
                    "when": [
                      "RightCellUp"
                    ],
                    "cards": [
                      {
                        "id": "l-launch-right",
                        "kind": "action",
                        "name": "LaunchAll",
                        "timeoutS": 1
                      },
                      {
                        "id": "l-hold-yes",
                        "kind": "firstOf",
                        "label": "R has left the scoring FLOWER: park at 22.7 s",
                        "rows": [
                          {
                            "afterMs": 22700,
                            "matchClock": true,
                            "cards": []
                          }
                        ]
                      },
                      {
                        "id": "l-c-park",
                        "kind": "path",
                        "lineId": "l-park",
                        "park": true
                      }
                    ]
                  }
                ]
              }
            ]
          }
        ]
      }
    ],
    "startAt": "LEFT_START",
    "linkPaths": [
      "link-1",
      "link-2",
      "link-3"
    ],
    "parkFrom": {
      "l-c-offset": "l-park",
      "l-c-pickup": "l-pickup-park",
      "l-c-open": "l-catch-park"
    },
    "exportName": "L-Clockwise-Garden"
  }
}