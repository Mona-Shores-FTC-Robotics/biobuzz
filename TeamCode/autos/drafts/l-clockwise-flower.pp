{
  "version": "2.0.0",
  "timestamp": "2026-11-11T18:20:37.322Z",
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
      "name": "open to LEFT_LAUNCH_CATCH",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 57.5,
        "y": 123.5
      },
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 270,
        "endDeg": 273.4
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
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 273.4,
        "endDeg": 360
      }
    },
    {
      "id": "link-1",
      "name": "Link (never driven)",
      "color": "#6b7280",
      "kind": "atomic",
      "endPoint": {
        "x": 57.5,
        "y": 123.5
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 273.4
      }
    },
    {
      "id": "l-rescue",
      "color": "#4e79a7",
      "name": "under the HIVE to RIGHT_LAUNCH_CATCH",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "compound",
      "segments": [
        {
          "id": "l-rescue-south",
          "color": "#4e79a7",
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
          }
        },
        {
          "id": "l-rescue-turn",
          "color": "#59a14f",
          "name": "",
          "locked": false,
          "waitBeforeMs": 0,
          "waitAfterMs": 0,
          "waitBeforeName": "",
          "waitAfterName": "",
          "kind": "atomic",
          "endPoint": {
            "x": 57.7,
            "y": 24
          },
          "controlPoints": [],
          "heading": {
            "type": "linear",
            "startDeg": 270,
            "endDeg": 91.9
          }
        },
        {
          "id": "l-rescue-aim",
          "color": "#f28e2b",
          "name": "",
          "locked": false,
          "waitBeforeMs": 0,
          "waitAfterMs": 0,
          "waitBeforeName": "",
          "waitAfterName": "",
          "kind": "atomic",
          "endPoint": {
            "x": 57.5,
            "y": 18
          },
          "controlPoints": [],
          "heading": {
            "type": "constant",
            "degrees": 91.9
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
              "x": 57.5,
              "y": 30
            },
            {
              "x": 56,
              "y": 38
            }
          ],
          "heading": {
            "type": "linear",
            "startDeg": 91.9,
            "endDeg": 90
          }
        },
        {
          "id": "l-rescue-park-west",
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
          "color": "#b07aa1",
          "name": "",
          "locked": false,
          "waitBeforeMs": 0,
          "waitAfterMs": 0,
          "waitBeforeName": "",
          "waitAfterName": "",
          "kind": "atomic",
          "endPoint": {
            "x": 33,
            "y": 114
          },
          "controlPoints": [
            {
              "x": 31,
              "y": 40
            },
            {
              "x": 33,
              "y": 95
            }
          ],
          "heading": {
            "type": "constant",
            "degrees": 90
          }
        },
        {
          "id": "l-rescue-park-turn",
          "color": "#f28e2b",
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
          "controlPoints": [
            {
              "x": 33,
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
        "x": 57.5,
        "y": 123.5
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 273.4
      }
    },
    {
      "id": "l-under",
      "color": "#4e79a7",
      "name": "under the HIVE to the lane's end",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 60.5,
        "y": 34
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
      }
    },
    {
      "id": "l-under-park",
      "color": "#4e79a7",
      "name": "lane's end park to LEFT_PARK",
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
          "x": 33,
          "y": 40
        },
        {
          "x": 33,
          "y": 100
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
                "startDeg": 270,
                "endDeg": 90
              }
            },
            {
              "startProgress": 0.3,
              "endProgress": 0.8,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 90
              }
            },
            {
              "startProgress": 0.8,
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
      "id": "link-3",
      "name": "Link (never driven)",
      "color": "#6b7280",
      "kind": "atomic",
      "endPoint": {
        "x": 60.5,
        "y": 34
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": -90
      }
    },
    {
      "id": "l-pickup",
      "color": "#4e79a7",
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
      "controlPoints": [
        {
          "x": 52,
          "y": 38
        },
        {
          "x": 36,
          "y": 38
        },
        {
          "x": 33,
          "y": 47.2
        }
      ],
      "heading": {
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0,
              "endProgress": 0.5,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 270,
                "endDeg": 180
              }
            },
            {
              "startProgress": 0.5,
              "endProgress": 1,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 180
              }
            }
          ]
        }
      }
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
          "color": "#edc948",
          "name": "",
          "locked": false,
          "waitBeforeMs": 0,
          "waitAfterMs": 0,
          "waitBeforeName": "",
          "waitAfterName": "",
          "kind": "atomic",
          "endPoint": {
            "x": 33,
            "y": 114
          },
          "controlPoints": [
            {
              "x": 32,
              "y": 62
            },
            {
              "x": 33,
              "y": 95
            }
          ],
          "heading": {
            "type": "piecewise",
            "piecewiseHeading": {
              "segments": [
                {
                  "startProgress": 0,
                  "endProgress": 0.25,
                  "interpolationType": "linear",
                  "parameters": {
                    "startDeg": 180,
                    "endDeg": 90
                  }
                },
                {
                  "startProgress": 0.25,
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
          "controlPoints": [
            {
              "x": 33,
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
          }
        }
      ]
    },
    {
      "id": "link-4",
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
      "id": "l-to-offset",
      "color": "#4e79a7",
      "name": "FLOWER to RIGHT_LAUNCH_OFFSET",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 41,
        "y": 20
      },
      "controlPoints": [
        {
          "x": 30,
          "y": 40
        },
        {
          "x": 33,
          "y": 22
        }
      ],
      "heading": {
        "type": "linear",
        "startDeg": 180,
        "endDeg": 68.7
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
          "color": "#f28e2b",
          "name": "",
          "locked": false,
          "waitBeforeMs": 0,
          "waitAfterMs": 0,
          "waitBeforeName": "",
          "waitAfterName": "",
          "kind": "atomic",
          "endPoint": {
            "x": 33,
            "y": 114
          },
          "controlPoints": [
            {
              "x": 25,
              "y": 20
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
          }
        },
        {
          "id": "l-park-turn",
          "color": "#9c755f",
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
          "controlPoints": [
            {
              "x": 33,
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
      "lineId": "l-rescue-park-turn"
    },
    {
      "kind": "path",
      "lineId": "link-2"
    },
    {
      "kind": "path",
      "lineId": "l-under"
    },
    {
      "kind": "path",
      "lineId": "l-under-park"
    },
    {
      "kind": "path",
      "lineId": "link-3"
    },
    {
      "kind": "path",
      "lineId": "l-pickup"
    },
    {
      "kind": "path",
      "lineId": "l-pickup-park-lane"
    },
    {
      "kind": "path",
      "lineId": "l-pickup-park-turn"
    },
    {
      "kind": "path",
      "lineId": "link-4"
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
      "LEFT_LAUNCH_CATCH": [
        57.5,
        123.5,
        273.4
      ],
      "RIGHT_LAUNCH_CATCH": [
        57.5,
        18,
        91.9
      ],
      "WALL_FLOWER_STAGING": [
        23.1,
        47.2,
        180
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
      "l-catch-park": "LEFT_PARK",
      "l-rescue-aim": "RIGHT_LAUNCH_CATCH",
      "l-rescue-park-turn": "LEFT_PARK",
      "l-under-park": "LEFT_PARK",
      "l-pickup": "WALL_FLOWER_STAGING",
      "l-to-offset": "RIGHT_LAUNCH_OFFSET",
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
                "label": "R has parked from the left spot: park at 24.6 s",
                "rows": [
                  {
                    "afterMs": 24600,
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
                "id": "l-c-under",
                "kind": "path",
                "lineId": "l-under",
                "park": false
              },
              {
                "id": "l-hold-flower",
                "kind": "firstOf",
                "label": "R has passed the FLOWER: go at 15.5 s",
                "rows": [
                  {
                    "afterMs": 15500,
                    "matchClock": true,
                    "cards": []
                  }
                ]
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
                "name": "PickFlower",
                "timeoutS": 4.5
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
      "link-3",
      "link-4"
    ],
    "parkFrom": {
      "l-c-under": "l-under-park",
      "l-c-offset": "l-park",
      "l-c-pickup": "l-pickup-park",
      "l-c-open": "l-catch-park"
    },
    "exportName": "L-Clockwise-Flower"
  }
}