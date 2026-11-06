{
  "version": "2.0.0",
  "timestamp": "2026-11-06T13:04:21.706Z",
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
      "kind": "atomic",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "id": "l-flower",
      "name": "flower to SCORING_FLOWER_STAGING",
      "endPoint": {
        "x": 47.2,
        "y": 119.3
      },
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 273.4,
        "endDeg": 90
      },
      "color": "#4e79a7"
    },
    {
      "kind": "atomic",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "id": "l-under-hive-south",
      "name": "under hive south to RIGHT_LAUNCH_CLOSE",
      "endPoint": {
        "x": 56.1,
        "y": 26
      },
      "controlPoints": [
        {
          "x": 64,
          "y": 116
        },
        {
          "x": 61,
          "y": 102
        },
        {
          "x": 60,
          "y": 47
        },
        {
          "x": 56.1,
          "y": 37
        }
      ],
      "heading": {
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0,
              "endProgress": 0.18,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 90,
                "endDeg": 270
              }
            },
            {
              "startProgress": 0.18,
              "endProgress": 0.8,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 270
              }
            },
            {
              "startProgress": 0.8,
              "endProgress": 1,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 270,
                "endDeg": 90
              }
            }
          ]
        }
      },
      "color": "#4e79a7"
    },
    {
      "kind": "atomic",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "id": "l-under-hive-north-park",
      "name": "under hive north park to LEFT_PARK",
      "endPoint": {
        "x": 14.5,
        "y": 117.4
      },
      "controlPoints": [
        {
          "x": 60,
          "y": 38
        },
        {
          "x": 60,
          "y": 80
        },
        {
          "x": 65,
          "y": 111
        },
        {
          "x": 57,
          "y": 127
        },
        {
          "x": 30,
          "y": 125
        }
      ],
      "heading": {
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0,
              "endProgress": 0.15,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 90
              }
            },
            {
              "startProgress": 0.15,
              "endProgress": 0.66,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 90
              }
            },
            {
              "startProgress": 0.66,
              "endProgress": 1,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 90,
                "endDeg": 0
              }
            }
          ]
        }
      },
      "color": "#4e79a7"
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
      "controlPoints": [
        {
          "x": 44,
          "y": 121
        },
        {
          "x": 27,
          "y": 120
        }
      ],
      "heading": {
        "type": "linear",
        "startDeg": 273.4,
        "endDeg": 0
      },
      "color": "#4e79a7"
    },
    {
      "kind": "atomic",
      "id": "l-link-flower-abort-park",
      "name": "Link to SCORING_FLOWER_STAGING",
      "endPoint": {
        "x": 47.2,
        "y": 119.3
      },
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 0,
        "endDeg": 90
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
      "id": "l-flower-abort-park",
      "name": "flower abort park to LEFT_PARK",
      "endPoint": {
        "x": 14.5,
        "y": 117.4
      },
      "controlPoints": [
        {
          "x": 43,
          "y": 109
        },
        {
          "x": 28,
          "y": 107
        }
      ],
      "heading": {
        "type": "linear",
        "startDeg": 90,
        "endDeg": 0
      },
      "color": "#4e79a7"
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
      "SCORING_FLOWER_STAGING": [
        47.2,
        119.3,
        90
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
      ]
    },
    "pathEnds": {
      "l-open": "LEFT_LAUNCH_CATCH",
      "l-flower": "SCORING_FLOWER_STAGING",
      "l-under-hive-south": "RIGHT_LAUNCH_CLOSE",
      "l-under-hive-north-park": "LEFT_PARK",
      "l-left-abort-park": "LEFT_PARK",
      "l-flower-abort-park": "LEFT_PARK"
    },
    "cards": [
      {
        "id": "L-action-5",
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
        "id": "L-wait-7",
        "kind": "firstOf",
        "label": "LEFT up by 8 s",
        "rows": [
          {
            "afterMs": 8000,
            "matchClock": true,
            "cards": [
              {
                "id": "l-clear-wall-lane",
                "kind": "firstOf",
                "label": "R clears LEFT shooting lane by 21 s",
                "rows": [
                  {
                    "afterMs": 21000,
                    "matchClock": true,
                    "cards": []
                  }
                ]
              },
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
                "id": "L-action-6",
                "kind": "action",
                "name": "LaunchAll"
              },
              {
                "id": "L-wait-4",
                "kind": "firstOf",
                "label": "TIP 2 starts by 16 s: flower is released",
                "rows": [
                  {
                    "afterMs": 16000,
                    "matchClock": true,
                    "cards": [
                      {
                        "id": "l-clear-wall-lane-alt",
                        "kind": "firstOf",
                        "label": "R clears LEFT shooting lane by 21 s",
                        "rows": [
                          {
                            "afterMs": 21000,
                            "matchClock": true,
                            "cards": []
                          }
                        ]
                      },
                      {
                        "id": "card-l-left-abort-park-alt",
                        "kind": "path",
                        "lineId": "l-left-abort-park",
                        "park": true
                      }
                    ]
                  },
                  {
                    "when": [
                      "Tip"
                    ],
                    "cards": [
                      {
                        "id": "card-l-flower",
                        "kind": "path",
                        "lineId": "l-flower",
                        "park": false
                      },
                      {
                        "id": "L-action-3",
                        "kind": "action",
                        "name": "PickFlower",
                        "timeoutS": 4.5
                      },
                      {
                        "id": "card-l-under-hive-south",
                        "kind": "path",
                        "lineId": "l-under-hive-south",
                        "park": false
                      },
                      {
                        "id": "L-wait-2",
                        "kind": "firstOf",
                        "label": "RIGHT up before 21.5 s, otherwise park",
                        "rows": [
                          {
                            "afterMs": 21500,
                            "matchClock": true,
                            "cards": [
                              {
                                "id": "card-l-under-hive-north-park",
                                "kind": "path",
                                "lineId": "l-under-hive-north-park",
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
                                "id": "card-l-under-hive-north-park-alt",
                                "kind": "path",
                                "lineId": "l-under-hive-north-park",
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
            ]
          }
        ]
      }
    ],
    "startAt": "LEFT_START",
    "exportName": "L-Infraviolet",
    "parkFrom": {
      "card-l-open": "l-left-abort-park",
      "card-l-flower": "l-flower-abort-park",
      "card-l-under-hive-south": "l-under-hive-north-park"
    }
  }
}
