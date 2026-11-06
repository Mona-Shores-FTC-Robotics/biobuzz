{
  "version": "2.0.0",
  "timestamp": "2026-11-06T14:21:32.658Z",
  "startPoint": {
    "x": 62.3,
    "y": 133.8,
    "headingDeg": 270,
    "locked": false
  },
  "lines": [
    {
      "kind": "atomic",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "id": "l-open",
      "name": "open to LEFT_LAUNCH_CATCH",
      "endPoint": {
        "x": 57.5,
        "y": 123.5
      },
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 270,
        "endDeg": 273.4
      },
      "color": "#4e79a7"
    },
    {
      "kind": "compound",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "id": "l-early-garden",
      "name": "early garden to GARDEN_STAGING",
      "color": "#4e79a7",
      "segments": [
        {
          "kind": "atomic",
          "id": "l-empty-under-hive",
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
            "piecewiseHeading": {
              "segments": [
                {
                  "startProgress": 0,
                  "endProgress": 0.1,
                  "interpolationType": "linear",
                  "parameters": {
                    "startDeg": 273.4,
                    "endDeg": 270
                  }
                },
                {
                  "startProgress": 0.1,
                  "endProgress": 1,
                  "interpolationType": "constant",
                  "parameters": {
                    "degrees": 270
                  }
                }
              ]
            }
          },
          "locked": false
        },
        {
          "kind": "atomic",
          "id": "l-empty-to-garden",
          "endPoint": {
            "x": 11.5,
            "y": 22.1
          },
          "controlPoints": [],
          "heading": {
            "type": "constant",
            "degrees": 270
          },
          "locked": false
        }
      ]
    },
    {
      "kind": "atomic",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "id": "l-garden-forward-exit",
      "name": "garden forward exit to RIGHT_LAUNCH_OFFSET",
      "endPoint": {
        "x": 41,
        "y": 20
      },
      "controlPoints": [
        {
          "x": 11.5,
          "y": 10
        },
        {
          "x": 34,
          "y": 10
        },
        {
          "x": 37.36,
          "y": 10.67
        }
      ],
      "heading": {
        "type": "tangential",
        "reverse": false
      },
      "color": "#4e79a7"
    },
    {
      "kind": "compound",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "id": "l-wall-lane-far-park",
      "name": "wall lane far park to LEFT_PARK",
      "color": "#4e79a7",
      "segments": [
        {
          "kind": "atomic",
          "id": "l-park-past-near-robot",
          "endPoint": {
            "x": 35,
            "y": 114
          },
          "controlPoints": [
            {
              "x": 35,
              "y": 35
            },
            {
              "x": 35,
              "y": 95
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
                    "startDeg": 68.7,
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
          },
          "locked": false
        },
        {
          "kind": "atomic",
          "id": "l-far-park-turn",
          "endPoint": {
            "x": 14.5,
            "y": 117.4
          },
          "controlPoints": [
            {
              "x": 35,
              "y": 124
            },
            {
              "x": 27,
              "y": 124
            }
          ],
          "heading": {
            "type": "linear",
            "startDeg": 90,
            "endDeg": 0
          },
          "locked": false
        }
      ]
    },
    {
      "kind": "atomic",
      "id": "l-link-left-abort-park",
      "name": "Link to LEFT_LAUNCH_CATCH",
      "endPoint": {
        "x": 57.5,
        "y": 123.5
      },
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 0,
        "endDeg": 273.4
      },
      "locked": false
    },
    {
      "kind": "atomic",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "id": "l-left-abort-park",
      "name": "left abort park to LEFT_PARK",
      "endPoint": {
        "x": 14.5,
        "y": 117.4
      },
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 273.4,
        "endDeg": 0
      },
      "color": "#4e79a7"
    },
    {
      "kind": "atomic",
      "id": "l-link-garden-abort-park",
      "name": "Link to GARDEN_STAGING",
      "endPoint": {
        "x": 11.5,
        "y": 22.1
      },
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 0,
        "endDeg": 270
      },
      "locked": false
    },
    {
      "kind": "compound",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "id": "l-garden-abort-park",
      "name": "garden abort park to LEFT_PARK",
      "color": "#4e79a7",
      "segments": [
        {
          "kind": "atomic",
          "id": "l-garden-park-past-near-robot",
          "endPoint": {
            "x": 35,
            "y": 114
          },
          "controlPoints": [
            {
              "x": 35,
              "y": 38
            },
            {
              "x": 35,
              "y": 95
            }
          ],
          "heading": {
            "type": "linear",
            "startDeg": 270,
            "endDeg": 90
          },
          "locked": false
        },
        {
          "kind": "atomic",
          "id": "l-garden-far-park-turn",
          "endPoint": {
            "x": 14.5,
            "y": 117.4
          },
          "controlPoints": [
            {
              "x": 35,
              "y": 124
            },
            {
              "x": 27,
              "y": 124
            }
          ],
          "heading": {
            "type": "linear",
            "startDeg": 90,
            "endDeg": 0
          },
          "locked": false
        }
      ]
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
  "sequence": [],
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
        "CameraBlind"
      ],
      "events": [
        "Tip"
      ],
      "typicalS": {
        "LaunchAll": 4.5,
        "LaunchOne": 0.5,
        "SpinUp": 0.1,
        "PickFlower": 3.7,
        "GardenGather": 5.0
      }
    },
    "points": {
      "LEFT_START": [
        62.3,
        133.8,
        270
      ],
      "LEFT_LAUNCH_CATCH": [
        57.5,
        123.5,
        273.4
      ],
      "GARDEN_STAGING": [
        11.5,
        22.1,
        270
      ],
      "RIGHT_LAUNCH_OFFSET": [
        41,
        20,
        68.7
      ],
      "LEFT_PARK": [
        14.5,
        117.4,
        0
      ]
    },
    "pathEnds": {
      "l-open": "LEFT_LAUNCH_CATCH",
      "l-early-garden": "GARDEN_STAGING",
      "l-garden-forward-exit": "RIGHT_LAUNCH_OFFSET",
      "l-wall-lane-far-park": "LEFT_PARK",
      "l-left-abort-park": "LEFT_PARK",
      "l-garden-abort-park": "LEFT_PARK"
    },
    "cards": [
      {
        "id": "L-action-3",
        "kind": "action",
        "name": "SpinUp"
      },
      {
        "id": "card-l-open",
        "kind": "path",
        "lineId": "l-open",
        "park": false
      },
      {
        "id": "L-wait-6",
        "kind": "firstOf",
        "label": "TIP 1 settled: LEFT up before 8 s",
        "rows": [
          {
            "afterMs": 8000,
            "matchClock": true,
            "cards": [
              {
                "id": "card-l-left-abort-park",
                "kind": "path",
                "lineId": "l-left-abort-park",
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
                "id": "L-action-4",
                "kind": "action",
                "name": "LaunchAll",
                "timeoutS": 1
              },
              {
                "id": "card-l-early-garden",
                "kind": "path",
                "lineId": "l-early-garden",
                "park": false
              },
              {
                "id": "L-action-5",
                "kind": "action",
                "name": "GardenGather",
                "timeoutS": 5.5
              },
              {
                "id": "card-l-garden-forward-exit",
                "kind": "path",
                "lineId": "l-garden-forward-exit",
                "park": false
              },
              {
                "id": "L-wait-2",
                "kind": "firstOf",
                "label": "TIP 2 settled: RIGHT up before 20 s",
                "rows": [
                  {
                    "afterMs": 20000,
                    "matchClock": true,
                    "cards": [
                      {
                        "id": "card-l-wall-lane-far-park",
                        "kind": "path",
                        "lineId": "l-wall-lane-far-park",
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
                        "id": "L-action-1",
                        "kind": "action",
                        "name": "LaunchAll",
                        "timeoutS": 1
                      },
                      {
                        "id": "card-l-wall-lane-far-park-alt",
                        "kind": "path",
                        "lineId": "l-wall-lane-far-park",
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
    "exportName": "L-Infraviolet",
    "parkFrom": {
      "card-l-open": "l-left-abort-park",
      "card-l-early-garden": "l-garden-abort-park",
      "card-l-garden-forward-exit": "l-wall-lane-far-park"
    }
  }
}
